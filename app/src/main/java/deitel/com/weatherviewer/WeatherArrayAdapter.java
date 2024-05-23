package deitel.com.weatherviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherArrayAdapter extends ArrayAdapter<Weather> {

    private static class ViewHolder {
        ImageView conditionImageView;
        TextView dayTextView;
        TextView lowTextView;
        TextView hiTextView;
        TextView humidityTextView;
    }

    // Almacena mapas de bits ya descargados para su reutilización
    private Map<String, Bitmap> bitmaps = new HashMap<>();

    // Constructor para inicializar los miembros heredados de la superclase
    public WeatherArrayAdapter(Context context, List<Weather> forecast) {
        super(context, -1, forecast);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Obtener el objeto Weather para esta posición de ListView especificada
        Weather day = getItem(position);

        ViewHolder viewHolder; // Referenciar las vistas del elemento de la lista

        // Verificar si hay un ViewHolder reutilizable de un elemento ListView que se desplazó fuera de pantalla; de lo contrario, crear uno nuevo
        if (convertView == null) { // No hay ViewHolder reutilizable, así que crea uno
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            viewHolder.conditionImageView = convertView.findViewById(R.id.conditionImageView);
            viewHolder.dayTextView = convertView.findViewById(R.id.dayTextView);
            viewHolder.lowTextView = convertView.findViewById(R.id.lowTextView);
            viewHolder.hiTextView = convertView.findViewById(R.id.hiTextView);
            viewHolder.humidityTextView = convertView.findViewById(R.id.humidityTextView);
            convertView.setTag(viewHolder);
        } else { // Reutilizar ViewHolder existente almacenado como la etiqueta del elemento de la lista
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Si el ícono de condiciones climáticas ya se descargó, utilícelo; de lo contrario, descargue el ícono en un hilo separado
        if (bitmaps.containsKey(day.iconURL)) {
            viewHolder.conditionImageView.setImageBitmap(bitmaps.get(day.iconURL));
        } else {
            // Descargar y mostrar la imagen de las condiciones meteorológicas
            new LoadImageTask(viewHolder.conditionImageView).execute(day.iconURL);
        }

        // Obtener otros datos del objeto Weather y colocarlos en las vistas
        Context context = getContext(); // Para cargar recursos de cadena
        viewHolder.dayTextView.setText(context.getString(R.string.descripción_del_día, day.dayOfWeek, day.description));
        viewHolder.lowTextView.setText(context.getString(R.string.baja_temperatura, day.minTemp));
        viewHolder.hiTextView.setText(context.getString(R.string.alta_temperatura, day.maxTemp));
        viewHolder.humidityTextView.setText(context.getString(R.string.humedad, day.humidity));

        return convertView; // Devuelve el elemento de la lista completado para mostrar
    }

    // AsyncTask para cargar íconos de condiciones climáticas en un hilo separado
    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView; // Muestra la miniatura

        // Almacenar ImageView en el que establecer el mapa de bits descargado
        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        // Cargar imagen; params[0] es la URL de cadena que representa la imagen
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(params[0]); // Crear URL para la imagen

                // Abre una HttpURLConnection, obtiene su InputStream y descarga la imagen
                connection = (HttpURLConnection) url.openConnection();

                try (InputStream inputStream = connection.getInputStream()) {
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    bitmaps.put(params[0], bitmap); // Caché para uso posterior
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect(); // Cerrar HttpURLConnection
                }
            }

            return bitmap;
        }

        // Establece la imagen de la condición climática en el elemento de la lista
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }
}
