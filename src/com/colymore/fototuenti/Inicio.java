package com.colymore.fototuenti;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class Inicio extends Activity {

	private Context contexto;

	private EditText etEmail;
	private EditText etPassword;
	private Button btnDescargar;
	private Gallery listaHorizontalImagenes;

	private String email;
	private String password;
	private String cookieSesion;
	private StringBuilder body;
	private String csrf;
	private String pid;
	private String enlaceFotoSiguiente;
	public ArrayList<String> pathImagenes = new ArrayList<String>();

	private int numeroDeFotos;
	private ArrayList<Integer> imagenesConError;

	private File directorioFotos = Environment.getExternalStorageDirectory();

	private HttpURLConnection Conexion;

	private List<String> cookies;
	private List<Cookie> listaCookies;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_incio);
		
		 if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
	            ActionBar actionBar = getActionBar();
	            actionBar.hide();

	        }

		contexto = getApplicationContext();
		// Layout
		etEmail = (EditText) findViewById(R.id.etEmail);

		etPassword = (EditText) findViewById(R.id.etPassword);

		listaHorizontalImagenes = (Gallery) findViewById(R.id.galeria);
		listaHorizontalImagenes.setGravity(Gravity.LEFT);

		btnDescargar = (Button) findViewById(R.id.btnDescargar);
		btnDescargar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				email = etEmail.getText().toString();
				password = etPassword.getText().toString();

				if (validaDatos(email, password)) {
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					new DescargaFotos().execute();
				}
			}
		});

	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);

	}

	/**
	 * Comprobar que no introduce datos nulos
	 * 
	 * @param email
	 *            Email.
	 * @param password
	 *            Password.
	 * @return True si correcto False si no,
	 */
	private Boolean validaDatos(String email, String password) {

		if (email == null && email.isEmpty() || password == null
				|| password.isEmpty()) {
			return false;
		}
		return true;

	}

	/****** Metodos Secundarios ****/

	/**
	 * Metodo para recibir por get una url
	 * 
	 * @param url
	 *            A la cual hacemos la peticion
	 * 
	 * @return true si correcto false si no
	 */
	private Boolean getWeb(URL url) {

		try {
			Conexion = (HttpURLConnection) url.openConnection();
			Conexion.setDoInput(true);
			Conexion.setConnectTimeout(40000);
			Conexion.setDoOutput(true);
			Conexion.setUseCaches(false);
			Conexion.setDefaultUseCaches(false);
			Conexion.setRequestProperty("Accept", "*/*");
			Conexion.setRequestProperty(
					"User-agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:12.0) Gecko/20100101 Firefox/12.0");
			InputStream in = Conexion.getInputStream();
			String encoding = Conexion.getContentEncoding();
			encoding = encoding == null ? "UTF-8" : encoding;

			body = inputStreamaBody(in);
			cookies = Conexion.getHeaderFields().get("Set-Cookie");

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Metodo para recibir por get una url pasandole una cookie
	 * 
	 * @param url
	 *            A la cual hacemos la peticion
	 * @param cookie
	 *            Cookie que se le quiere enviar
	 * @return true si correcto false si no
	 */
	private Boolean getWeb(URL url, String cookie) {

		try {
			Conexion = (HttpURLConnection) url.openConnection();
			Conexion.setDoInput(true);
			Conexion.setConnectTimeout(40000);
			Conexion.setDoOutput(true);
			Conexion.setUseCaches(false);
			Conexion.setDefaultUseCaches(false);
			Conexion.setRequestProperty("Accept", "*/*");
			Conexion.setRequestProperty(
					"User-agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:12.0) Gecko/20100101 Firefox/12.0");
			Conexion.setRequestProperty("Cookie", cookie);
			InputStream in = Conexion.getInputStream();
			String encoding = Conexion.getContentEncoding();
			encoding = encoding == null ? "UTF-8" : encoding;

			body = inputStreamaBody(in);
			cookies = Conexion.getHeaderFields().get("Set-Cookie");

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Metodo para generar una peticion http Post
	 * 
	 * @param url
	 *            A la cual hacemos la peticion
	 * @param cookie
	 *            Cookie que se le quiere enviar
	 * @param postData
	 *            Parametros HTTP que enviaremos
	 * @return true si correcto false si no
	 */
	private Boolean postWeb(URL url, String cookie, List<NameValuePair> postData) {

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://m.tuenti.com/?m=Login&f=process_login");
		httppost.addHeader("Cookie", cookie);
		httppost.addHeader(
				"User-agent",
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:12.0) Gecko/20100101 Firefox/12.0");
		Log.i("Cookie enviada en get del login ", cookie);
		try {

			httppost.setEntity(new UrlEncodedFormEntity(postData));
			HttpResponse response = httpclient.execute(httppost);
			Reader reader = new InputStreamReader(response.getEntity()
					.getContent());
			StringBuffer sb = new StringBuffer();
			{
				int read;
				char[] cbuf = new char[1024];
				while ((read = reader.read(cbuf)) != -1)
					sb.append(cbuf, 0, read);
			}

			CookieStore cookieStore = ((AbstractHttpClient) httpclient)
					.getCookieStore();

			listaCookies = cookieStore.getCookies();

			String tuentiemail = null;
			String domain = null;
			String path = null;
			String expires = null;
			String mid = null;
			String lang = null;

			for (Cookie cookieEnLista : listaCookies) {

				try {

					domain = cookieEnLista.getDomain();
					path = cookieEnLista.getPath();
					expires = cookieEnLista.getExpiryDate().toString();

					int i = 0;
					if (cookieEnLista.getName().equals("tuentiemail"))
						i = 0;
					else if (cookieEnLista.getName().equals("mid"))
						i = 1;
					else if (cookieEnLista.getName().equals("lang"))
						i = 2;

					switch (i) {

					case 0:
						tuentiemail = cookieEnLista.getValue();
						break;
					case 1:
						mid = cookieEnLista.getValue();
						break;
					case 2:
						lang = cookieEnLista.getValue();
						break;
					default:

						break;
					}

				} catch (Exception e) {

				}

			}

			com.colymore.fototuenti.Cookie galleta = new com.colymore.fototuenti.Cookie(
					pid, tuentiemail, expires, path, domain, mid, lang);
			cookieSesion = galleta.getCookie();

			Log.i("Cookie de sesion ", cookieSesion);

		} catch (ClientProtocolException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Metodo para descargar una foto pasandole la URL
	 * 
	 * @param url
	 *            de la foto
	 * @return true si es correcto false si no
	 */
	private String getImagen(URL url, int i) {

		File ficheroImagen;

		try {

			// Hago un get al Bitmap y genero un byte[]
			Bitmap bitmap = BitmapFactory.decodeStream((InputStream) url
					.getContent());
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();

			// Comprimo en PNG
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

			// Genero el fichero, un FOS y escribo en el el byte[] y cierro
			ficheroImagen = new File(directorioFotos + "/FotosTuenti/" + email
					+ "/" + i + ".jpg");
			ficheroImagen.createNewFile();
			FileOutputStream fo = new FileOutputStream(ficheroImagen);
			fo.write(bytes.toByteArray());
			fo.close();

		} catch (IOException e) {
			imagenesConError.add(i);
			e.printStackTrace();
			Log.e("Error descargando imagen numero", String.valueOf(i));
			return null;
		}
		return ficheroImagen.getPath();
	}

	/**
	 * Metodo para encontrar un string dentro de otro a partir de string inicial
	 * y string final
	 * 
	 * @param totalCadena
	 *            Cadena en la que buscar el String
	 * @param inicioCadena
	 *            String incial para la busqueda
	 * @param finalCadena
	 *            String final de la busqueda
	 * @return String final con la cadena
	 */
	private String getCadenaEnString(String totalCadena, String inicioCadena,
			String finalCadena) {

		int posInicio = totalCadena.indexOf(inicioCadena);
		if (posInicio == -1)
			return "";
		int posFinal = totalCadena.indexOf(finalCadena, posInicio
				+ inicioCadena.length());
		totalCadena.substring(posFinal);
		String cadenaEncontrada = totalCadena.substring(posInicio
				+ inicioCadena.length(), posFinal);
		return cadenaEncontrada;

	}

	/**
	 * Metodo para generar un string a partir de un list<String>
	 * 
	 * @param listaString
	 *            Lista que contiene los strings
	 * @param caracter
	 *            String que separa cada elemento
	 * @return String que contiene los elementos separados por el caracter
	 */
	public static String implodeLista(List<Header> listaString, String caracter) {

		String salida = "";

		if (listaString.size() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(listaString.get(0));

			for (int i = 1; i < listaString.size(); i++) {
				sb.append(caracter);
				sb.append(listaString.get(i));
			}

			salida = sb.toString();
		}

		return salida;
	}

	/**
	 * Metodo para limpiar una URl de caracteres extraños
	 * 
	 * @param url
	 *            que queremos limpiar
	 * @return String url limpia
	 */
	public String eliminarAmpDeUrL(String url) {

		url = StringEscapeUtils.unescapeHtml4(url).replaceAll("[^\\x20-\\x7e]",
				"");

		return url;
	}

	/**
	 * Metodo para convertir un InputStream en un StringBuilder Util para Bodys
	 * muy largos
	 * 
	 * @param is
	 *            InputStream que contiene el texto
	 * @return body StringBuilder que contiene el body
	 * @throws IOException
	 */
	public static StringBuilder inputStreamaBody(InputStream is)
			throws IOException {

		StringBuilder body = new StringBuilder();
		byte[] buffer = new byte[2048];
		int length;
		while ((length = is.read(buffer)) != -1) {
			body.append(new String(buffer, 0, length));
		}
		is.close();

		return body;
	}

	/**
	 * Metodo para generar un zip de un directorio
	 * 
	 * @param directory
	 * @param zipfile
	 * @throws IOException
	 */
	public static void generaZip(File directorio, File zipfile)
			throws IOException {
		URI base = directorio.toURI();
		Deque<File> queue = new LinkedList<File>();
		queue.push(directorio);
		OutputStream out = new FileOutputStream(zipfile);
		Closeable res = out;
		try {
			ZipOutputStream zout = new ZipOutputStream(out);
			res = zout;
			while (!queue.isEmpty()) {
				directorio = queue.pop();
				for (File kid : directorio.listFiles()) {
					String name = base.relativize(kid.toURI()).getPath();
					if (kid.isDirectory()) {
						queue.push(kid);
						name = name.endsWith("/") ? name : name + "/";
						zout.putNextEntry(new ZipEntry(name));
					} else {
						zout.putNextEntry(new ZipEntry(name));
						copy(kid, zout);
						zout.closeEntry();
					}
				}
			}
		} finally {
			res.close();
		}
	}

	private static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		while (true) {
			int readCount = in.read(buffer);
			if (readCount < 0) {
				break;
			}
			out.write(buffer, 0, readCount);
		}
	}

	private static void copy(File file, OutputStream out) throws IOException {
		InputStream in = new FileInputStream(file);
		try {
			copy(in, out);
		} finally {
			in.close();
		}
	}

	/************ Clases ***********/

	private class DescargaFotos extends AsyncTask<Void, Integer, Integer> {

		ProgressBar pb = (ProgressBar) findViewById(R.id.barraprogreso);
		AdaptadorImagenes adaptador = new AdaptadorImagenes(contexto);
		boolean correcto = true;

		@Override
		protected void onPreExecute() {

			listaHorizontalImagenes.setAdapter(adaptador);
			listaHorizontalImagenes.setSelection(0);
			btnDescargar.setOnClickListener(null);
			final float[] roundedCorners = new float[] { 2, 2, 2, 2, 2, 2, 2, 2 };
			ShapeDrawable pgDrawable = new ShapeDrawable(new RoundRectShape(
					roundedCorners, null, null));
			String colorAzul = "#0099CC";
			pgDrawable.getPaint().setColor(Color.parseColor(colorAzul));
			ClipDrawable progresoD = new ClipDrawable(pgDrawable, Gravity.LEFT,
					ClipDrawable.HORIZONTAL);
			pb.setProgressDrawable(progresoD);
			
			imagenesConError = new ArrayList<Integer>();

		}

		@Override
		protected Integer doInBackground(Void... params) {

			String pathImagen;

			setProgress(0);

			// Primero recogo el pid de la cookie inicial y el csrf
			try {
				getWeb(new URL("http://m.tuenti.com/?m=Login"));
				cookies.get(0);
				pid = getCadenaEnString(cookies.get(1), "pid=", ";");
				cookieSesion = "cookiename=1; " + "pid=" + pid;

				Log.i("Cookie inicial: ", cookieSesion);

				String cadenaInicioBusqueda = "name=\"csrf\" value=\"";
				String cadenaFinalBusqueda = "\"/>";

				csrf = getCadenaEnString(body.toString(), cadenaInicioBusqueda,
						cadenaFinalBusqueda);

			} catch (MalformedURLException e) {
				Log.e("Url Mal formada", e.getMessage());
				e.printStackTrace();
			}

			// Me logueo
			List<NameValuePair> postArgs = new ArrayList<NameValuePair>(2);
			postArgs.add(new BasicNameValuePair("csrf", csrf));
			postArgs.add(new BasicNameValuePair("tuentiemailaddress", email));
			postArgs.add(new BasicNameValuePair("password", password));
			postArgs.add(new BasicNameValuePair("remember", "1"));

			try {
				postWeb(new URL("http://m.tuenti.com/?m=Login&f=process_login"),
						cookieSesion, postArgs);

			} catch (MalformedURLException e) {
				Log.e("Url Mal formada", e.getMessage());
				e.printStackTrace();
			}

			// Voy al perfil
			try {
				// Busco el link de las fotos
				getWeb(new URL("http://m.tuenti.com/?m=Profile&func=my_profile"),
						cookieSesion);
				String cadenaInicioBusqueda = "<div class=\"h\">Fotos</div><a id=\"photos\"></a><div class=\"item\"><div> <small> <a href=\"";
				String cadenaFinalBusqueda = "\">";
				String urlAlbum = "http://m.tuenti.com/"
						+ getCadenaEnString(body.toString(),
								cadenaInicioBusqueda, cadenaFinalBusqueda);

				if (urlAlbum.equals("http://m.tuenti.com/")) {
					try {
						Log.i("Asyntask", "Hilo cancelado");
						this.cancel(true);

					} catch (Throwable e) {
						Log.e("Error asyntask", "Error cancelando la tarea");
						e.printStackTrace();
					}
				}

				if (!isCancelled()) {
					// Entro a la galeria y busco fotos en las que salgo
					getWeb(new URL(urlAlbum), cookieSesion);
					cadenaInicioBusqueda = "</h1><div class=\"item\"><a class=\"thumb\" href=\"";
					cadenaFinalBusqueda = "\">";
					String primeraFoto = null;

					primeraFoto = eliminarAmpDeUrL("http://m.tuenti.com/"
							+ getCadenaEnString(body.toString(),
									cadenaInicioBusqueda, cadenaFinalBusqueda));

					// Recogo el contenido del album, numero de fotos, enlace de
					// foto 2
					// y sucesivas
					getWeb(new URL(primeraFoto), cookieSesion);
					numeroDeFotos = Integer.valueOf(getCadenaEnString(
							body.toString(), "1 de ", ")"));

					pb.setMax(numeroDeFotos);
					cadenaInicioBusqueda = ") <a href=\"";
					cadenaFinalBusqueda = "\">Siguiente";

					enlaceFotoSiguiente = eliminarAmpDeUrL("http://m.tuenti.com/"
							+ getCadenaEnString(body.toString(),
									cadenaInicioBusqueda, cadenaFinalBusqueda));

					// Creo el directorio en el que se guardaran las fotos
					if (new File(directorioFotos + "/FotosTuenti/" + email)
							.exists()) {
						Log.e("Ficheros", "Directorio existente");
					} else {
						if (!new File(directorioFotos.toString()
								+ "/FotosTuenti/" + email).mkdirs()) {

							Log.e("Error Ficheros",
									"Error creando directorio o ya existe");

						} else {

							Log.i("Ficheros", "Directorio creado");
						}
					}

					// Busco enlace de la primera foto y la descargo y genero el
					// Image
					getWeb(new URL(primeraFoto), cookieSesion);
					cadenaInicioBusqueda = "\"thumb fullSize\"><img src=\"";
					cadenaFinalBusqueda = "\"";
					String jpgPrimeraFoto = null;

					jpgPrimeraFoto = eliminarAmpDeUrL(getCadenaEnString(
							body.toString(), cadenaInicioBusqueda,
							cadenaFinalBusqueda));

					pathImagen = getImagen(new URL(jpgPrimeraFoto), 1);
					pathImagenes.add(pathImagen);
					Log.i("Descargada imagen", "1");

					// En el progressupdate lo actualizo

					publishProgress(0);

					// Primera imagen descargada, descargo el resto

					for (int i = 2; i <= numeroDeFotos; i++) {

						getWeb(new URL(enlaceFotoSiguiente), cookieSesion);
						cadenaInicioBusqueda = ") <a href=\"";
						cadenaFinalBusqueda = "\">Siguiente";

						try {
							enlaceFotoSiguiente = eliminarAmpDeUrL(URLDecoder
									.decode("http://m.tuenti.com/"
											+ getCadenaEnString(
													body.toString(),
													cadenaInicioBusqueda,
													cadenaFinalBusqueda),
											"UTF-8"));
						} catch (UnsupportedEncodingException e) {
							Log.e("Error", e.getMessage());
							e.printStackTrace();
						}

						cadenaInicioBusqueda = "\"thumb fullSize\"><img src=\"";
						cadenaFinalBusqueda = "\"";
						String jpgFotoSiguiente = null;
						try {
							jpgFotoSiguiente = eliminarAmpDeUrL(URLDecoder
									.decode(getCadenaEnString(body.toString(),
											cadenaInicioBusqueda,
											cadenaFinalBusqueda), "UTF-8"));
						} catch (UnsupportedEncodingException e) {
							Log.e("Error", e.getMessage());
							e.printStackTrace();
						}

						pathImagen = getImagen(new URL(jpgFotoSiguiente), i);
						Log.i("Descargada imagen", String.valueOf(i));
						pathImagenes.add(pathImagen);
						publishProgress(i);

					}
				} else {
					correcto = false;
				}

			} catch (MalformedURLException e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}

			return 0;
		}

		@Override
		protected void onProgressUpdate(Integer... progreso) {

			// Actualizo el progreso
			pb.setProgress(progreso[0]);
			adaptador.addItem(progreso[0]);

		}

		@Override
		protected void onPostExecute(Integer numero) {

			if (!correcto) {
				Toast.makeText(contexto,
						"Error: Comprueba tu usuario y contraseña",
						Toast.LENGTH_LONG).show();
			} else {

				if (imagenesConError.size() != 0) {
					Toast.makeText(
							contexto,
							"Descarga completada con"
									+ String.valueOf(imagenesConError.size())
									+ " errores. podras encontrar tus fotos en /FotosTuenti/" + email, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(contexto, "Descarga completada, podras encontrar tus fotos en /FotosTuenti/" + email,
							Toast.LENGTH_LONG).show();
				}

				//V2
				/*AlertDialog.Builder builder = new AlertDialog.Builder(contexto);
				builder.setTitle("DropBox");
				builder.setMessage("¿Deseas generar un zip y almacenarlo en dropbox?");
				builder.setPositiveButton("SI", null);
				builder.setNegativeButton("NO", null);
				builder.show();*/
			}
		}
	}

	private class AdaptadorImagenes extends BaseAdapter {

		private Context contexto = null;
		private ArrayList<Integer> datos = new ArrayList<Integer>();

		public AdaptadorImagenes(Context contexto) {

			this.contexto = contexto;
		}

		public void addItem(Integer posicion) {

			datos.add(posicion);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return datos.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ImageView imagen = new ImageView(contexto);
			imagen.setImageBitmap(getBitMapRedimensionado(position));
			imagen.setScaleType(ScaleType.FIT_XY);
			return imagen;

		}

		/**
		 * Metodo para redimensionar un bimap
		 * 
		 * @param posicion
		 *            del bitmap en una lista que contiene los path
		 * @return bitmap redimensionado
		 */
		private Bitmap getBitMapRedimensionado(int posicion) {

			File fichero = new File(pathImagenes.get(posicion));
			Bitmap bmOriginal = redondeaEsquinasBitmap(Bitmap
					.createScaledBitmap(
							BitmapFactory.decodeFile(fichero.toString()), 210,
							210, true));

			return bmOriginal;
		}

		/**
		 * Metodo para redondear las esquinas de un bitmap
		 * 
		 * @param bitmap
		 *            a redondear
		 * @return bitmap redondeado
		 */
		public Bitmap redondeaEsquinasBitmap(Bitmap bitmap) {
			Bitmap salida = Bitmap.createBitmap(bitmap.getWidth(),
					bitmap.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(salida);

			final int color = 0xff424242;
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(),
					bitmap.getHeight());
			final RectF rectF = new RectF(rect);
			final float roundPx = 12;

			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			canvas.drawBitmap(bitmap, rect, rect, paint);

			return salida;
		}

	}

}
