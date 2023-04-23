package com.digitalram.myapp07032023

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import com.lowagie.text.Document
import com.lowagie.text.DocumentException
import com.lowagie.text.Font
import com.lowagie.text.FontFactory
import com.lowagie.text.HeaderFooter
import com.lowagie.text.Paragraph
import com.lowagie.text.Phrase
import com.lowagie.text.pdf.PdfWriter
import harmony.java.awt.Color
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.graphics.Bitmap.CompressFormat.PNG
import android.widget.Toast
import androidx.core.content.FileProvider
import com.lowagie.text.Image
import com.lowagie.text.pdf.PdfPTable

class MainActivity : AppCompatActivity() {
    
    var NOMBRE_DIRECTORIO = "MisPDFs"
    var NOMBRE_DOCUMENTO = "MiPDF.pdf"
    
    lateinit var etText : EditText

    var admin = AdminBD(this)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*admin.Ejecuta("INSERT INTO producto(nom_prod, existencia_prod, precio_prod) VALUES('Martillo', 4, 10.00)")
        admin.Ejecuta("INSERT INTO producto(nom_prod, existencia_prod, precio_prod) VALUES('Destornilador Plano', 10, 10.00)")
        admin.Ejecuta("INSERT INTO producto(nom_prod, existencia_prod, precio_prod) VALUES('Destornilador Cruz', 20, 20.00)")
        admin.Ejecuta("INSERT INTO producto(nom_prod, existencia_prod, precio_prod) VALUES('Taladro', 30, 30.00)")
        admin.Ejecuta("INSERT INTO producto(nom_prod, existencia_prod, precio_prod) VALUES('Sierra', 40, 40.00)")
        admin.Ejecuta("INSERT INTO producto(nom_prod, existencia_prod, precio_prod) VALUES('Serrucho', 50, 50.00)")*/

        etText = findViewById(R.id.etTexto)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1000)
        }
    }

    fun btnGenerarClick(view: View) {

        crearPDF()

        // Se crea una instancia de la clase File con la ruta del archivo que se desea abrir
        val file = File(Environment.getExternalStorageDirectory(), "Android/data/com.digitalram.myapp07032023/files/"+NOMBRE_DIRECTORIO + "/" + NOMBRE_DOCUMENTO)

        // Se utiliza FileProvider para obtener una Uri segura para el archivo
        val uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file) // Utilizar FileProvider para obtener la URI del archivo

        // Se crea una instancia de la clase Intent con la acción de visualización de contenido
        val intent = Intent(Intent.ACTION_VIEW)

        // Se establece la Uri del archivo y su tipo MIME en el Intent
        intent.setDataAndType(uri, "application/pdf")

        // Se otorgan permisos de lectura al Intent
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION // Otorgar permisos de lectura al Intent

        try {

            // Se intenta iniciar la actividad con el Intent para abrir el archivo en una aplicación externa
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {

            // Si no se encuentra ninguna aplicación en el dispositivo que pueda manejar archivos PDF,
            // se captura la excepción ActivityNotFoundException y se muestra un mensaje de Toast
            Toast.makeText(this, "No se encontró una aplicación para abrir archivos PDF en este dispositivo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun crearPDF() {

        var documento = Document()


        try {

            val file : File? = CrearFichero(NOMBRE_DOCUMENTO)
            val ficheroPDF = FileOutputStream(file!!.absoluteFile)

            val writer = PdfWriter.getInstance(documento, ficheroPDF)

            // Incluir Header
            val cabecera = HeaderFooter(Phrase("Reporte Ejemplo De PDF"), false)

            // Incluir Footer
            val pie = HeaderFooter(Phrase("Este Es Mi Pie De Pagina"), false)

            documento.setHeader(cabecera)
            documento.setFooter(pie)

            documento.open()
            documento.add(Paragraph("Titulo Con Fuente Default"))

            val font: Font = FontFactory.getFont(FontFactory.HELVETICA, 28.0f, Font.BOLD, Color.BLUE)
            documento.add(Paragraph("Titulo Con Helvetica", font))

            val bitmap : Bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.terminatorsfondo)
            val stream = ByteArrayOutputStream()

            bitmap.compress(PNG, 100, stream)

            val imagen : Image = Image.getInstance(stream.toByteArray())

            documento.add(imagen)

            documento.add(Paragraph("Tabla De Productos\n\n"))
            documento.add(
                Paragraph(
                    """
                        ${etText.text}
                        
                        """.trimIndent()
                )
            )

            // Insertamos una tabla
            var tabla = PdfPTable(3)

            var res = admin.Consulta("SELECT nom_prod, existencia_prod, precio_prod FROM producto ORDER BY precio_prod")

            tabla.addCell("Nombre Producto")
            tabla.addCell("Existencia")
            tabla.addCell("Precio")

            while (res!!.moveToNext()){

                var nom = res.getString(0).toString()
                var exi = res.getString(1).toString()
                var pre = res.getString(2).toString()

                tabla.addCell(nom)
                tabla.addCell(exi)
                tabla.addCell(pre)
            }
            documento.add(tabla)

        }catch (e: DocumentException){


        }catch (e: IOException){


        } finally {

            documento.close()

        }
    }

    private fun CrearFichero(nombreDocumento : String): File? {

        val ruta : File? = getRuta()
        var fichero : File? = null

        if (ruta != null){

            fichero = File(ruta, nombreDocumento)
        }

        return fichero
    }

    private fun getRuta(): File? {

        var ruta : File? = null

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            ruta = File(getExternalFilesDir(null), NOMBRE_DIRECTORIO)

            if (ruta != null) {

                if (!ruta.mkdirs()) {

                    if (!ruta.exists()) {

                        return null
                    }
                }
            }
        }

        return ruta
    }
}