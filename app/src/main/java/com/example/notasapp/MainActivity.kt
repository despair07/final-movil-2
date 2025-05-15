package com.example.notasapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notasapp.Adapter.NotesAdapter
import com.example.notasapp.Database.NoteDatabase
import com.example.notasapp.Models.Note
import com.example.notasapp.Models.NoteViewModel
import com.example.notasapp.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity(),
    NotesAdapter.NotesClickListener,
    NotesAdapter.AlarmClickListener,
    PopupMenu.OnMenuItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: NoteDatabase
    private lateinit var viewModel: NoteViewModel
    private lateinit var adapter: NotesAdapter
    private lateinit var selectedNote: Note

    private val updateNote = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val note = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getSerializableExtra("note", Note::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getSerializableExtra("note") as? Note
            }
            note?.let {
                viewModel.updateNote(it)
                val alarmTime = result.data?.getLongExtra("alarmTime", -1L) ?: -1L
                if (alarmTime > 0) {
                    scheduleAlarm(it, alarmTime)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        initUi()

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(NoteViewModel::class.java)

        viewModel.allnotes.observe(this) { noteList ->
            noteList?.let {
                adapter.updateList(it)
            }
        }

        database = NoteDatabase.getDatabase(this)
    }

    private fun initUi() {
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)

        adapter = NotesAdapter(this, this, this)
        binding.recyclerView.adapter = adapter

        val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val note = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data?.getSerializableExtra("note", Note::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.getSerializableExtra("note") as? Note
                }
                note?.let {
                    viewModel.insertNote(it)
                    val alarmTime = result.data?.getLongExtra("alarmTime", -1L) ?: -1L
                    if (alarmTime > 0) {
                        scheduleAlarm(it, alarmTime)
                    }
                }
            }
        }

        binding.fbAddNote.setOnClickListener {
            val intent = Intent(this, AddNote::class.java)
            getContent.launch(intent)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    adapter.filterList(it)
                }
                return true
            }
        })
    }

    override fun onItemClicked(note: Note) {
        val intent = Intent(this@MainActivity, AddNote::class.java)
        intent.putExtra("current_note", note)
        updateNote.launch(intent)
    }

    override fun onLongItemClicked(note: Note, cardView: CardView) {
        selectedNote = note
        popUpDisplay(cardView)
    }

    private fun popUpDisplay(cardView: CardView) {
        val popup = PopupMenu(this, cardView)
        popup.setOnMenuItemClickListener(this@MainActivity)
        popup.inflate(R.menu.pop_up_menu)
        popup.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.delete_note -> {
                viewModel.deleteNote(selectedNote)
                true
            }
            R.id.set_alarm -> {
                onAlarmClicked(selectedNote)
                true
            }
            else -> false
        }
    }

    // Solo muestra selector de HORA, y programa alarma para hoy o mañana si la hora ya pasó
    override fun onAlarmClicked(note: Note) {
        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance()

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // Si la hora ya pasó hoy, programar para mañana
            if (calendar.before(now)) {
                calendar.add(Calendar.DATE, 1)
            }

            scheduleAlarm(note, calendar.timeInMillis)
            Toast.makeText(this, "Alarma programada para las $hourOfDay:$minute", Toast.LENGTH_SHORT).show()
        }

        TimePickerDialog(
            this,
            timeSetListener,
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun scheduleAlarm(note: Note, timeInMillis: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("note_id", note.id)
            putExtra("note_title", note.title)
            putExtra("note_content", note.note)
        }

        val noteId = note.id ?: 0

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            noteId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        }

        Toast.makeText(this, "Alarma programada para la nota: ${note.title}", Toast.LENGTH_SHORT).show()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso para notificaciones concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permiso para notificaciones denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
