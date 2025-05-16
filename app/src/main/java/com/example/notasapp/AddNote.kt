package com.example.notasapp

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.notasapp.Models.Note
import com.example.notasapp.databinding.ActivityAddNoteBinding
import java.text.SimpleDateFormat
import java.util.*

class AddNote : AppCompatActivity() {

    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var note: Note
    private lateinit var old_note: Note
    var isUpdate = false

    // Variables para guardar la fecha y hora seleccionadas
    private var alarmCalendar: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Si recibimos una nota para editar
        val receivedNote = intent.getSerializableExtra("current_note")
        if (receivedNote != null && receivedNote is Note) {
            old_note = receivedNote
            binding.etTitle.setText(old_note.title)
            binding.etNote.setText(old_note.note)
            isUpdate = true
        }



        // Guardar nota y alarma
        binding.imgCheck.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val note_desc = binding.etNote.text.toString()

            if (title.isNotEmpty() || note_desc.isNotEmpty()) {
                val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm a", Locale.getDefault())

                note = if (isUpdate) {
                    Note(old_note.id, title, note_desc, formatter.format(Date()))
                } else {
                    Note(null, title, note_desc, formatter.format(Date()))
                }

                val intent = Intent()
                intent.putExtra("note", note)

                // Pasar el tiempo de la alarma en milisegundos si se seleccionó
                alarmCalendar?.timeInMillis?.let {
                    intent.putExtra("alarmTime", it)
                }

                setResult(Activity.RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this@AddNote, "Por favor ingrese datos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.imgBackArrow.setOnClickListener {
            onBackPressed()
        }
    }

    private fun showDateTimePicker() {
        val currentCalendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                // Al seleccionar la fecha, abrimos TimePicker
                alarmCalendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                showTimePicker()
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun showTimePicker() {
        val currentCalendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                alarmCalendar?.set(Calendar.HOUR_OF_DAY, hourOfDay)
                alarmCalendar?.set(Calendar.MINUTE, minute)
                alarmCalendar?.set(Calendar.SECOND, 0)
                alarmCalendar?.set(Calendar.MILLISECOND, 0)

                // Mostrar la fecha y hora seleccionada en el TextView
                alarmCalendar?.let {
                    val formatter = SimpleDateFormat("EEE, d MMM yyyy HH:mm a", Locale.getDefault())
                    binding.tvSetAlarm.text = "Alarma: ${formatter.format(it.time)}"
                }
            },
            currentCalendar.get(Calendar.HOUR_OF_DAY),
            currentCalendar.get(Calendar.MINUTE),
            false
        )
        timePicker.show()
    }
}
