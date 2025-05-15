package com.tuapp.utilities  // ✅ Asegúrate de que el paquete coincida con el tuyo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import java.util.*

object AlarmHelper {

    fun pickDateTime(context: Context, onDateTimeSelected: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()

        // Primero: seleccionar la fecha
        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            // Luego: seleccionar la hora
            TimePickerDialog(context, { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                onDateTimeSelected(calendar)

            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()

        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
}
