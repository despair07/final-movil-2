package com.example.notasapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.notasapp.Models.Note
import com.example.notasapp.R
import kotlin.random.Random

class NotesAdapter(
    private val context : Context,
    val listener: NotesClickListener,
    val alarmListener: AlarmClickListener  // <-- NUEVO callback para alarma
): RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    private val NotesList = ArrayList<Note>()
    private val fullList = ArrayList<Note>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return NotesList.size
    }

    fun updateList(newList: List<Note>) {
        fullList.clear()
        fullList.addAll(newList)

        NotesList.clear()
        NotesList.addAll(fullList)
        notifyDataSetChanged()
    }

    fun filterList(search: String) {
        NotesList.clear()

        for (item in fullList) {
            val titleContains = item.title?.lowercase()?.contains(search.lowercase()) == true
            val noteContains = item.note?.lowercase()?.contains(search.lowercase()) == true

            if (titleContains || noteContains) {
                NotesList.add(item)
            }
        }

        notifyDataSetChanged()
    }

    fun randomColor(): Int {
        val list = ArrayList<Int>()
        list.add(R.color.color1)
        list.add(R.color.color2)
        list.add(R.color.color3)
        list.add(R.color.color4)
        list.add(R.color.color5)
        list.add(R.color.color6)
        list.add(R.color.color7)
        list.add(R.color.color8)
        list.add(R.color.color9)
        list.add(R.color.color10)
        list.add(R.color.color11)
        list.add(R.color.color12)

        val seed = System.currentTimeMillis().toInt()
        val randomIndex = Random(seed).nextInt(list.size)
        return list[randomIndex]
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote = NotesList[position]

        holder.title.text = currentNote.title ?: "Sin título"
        holder.title.isSelected = true

        holder.note_tv.text = currentNote.note ?: "Sin contenido"
        holder.date.text = currentNote.date ?: "Sin fecha"
        holder.date.isSelected = true

        holder.notes_layout.setCardBackgroundColor(
            holder.itemView.resources.getColor(randomColor(), null)
        )

        // Click corto para editar
        holder.notes_layout.setOnClickListener {
            listener.onItemClicked(currentNote)
        }

        // Click largo para mostrar opciones eliminar o agregar alarma
        holder.notes_layout.setOnLongClickListener { view ->
            val popupMenu = PopupMenu(context, view)
            popupMenu.menuInflater.inflate(R.menu.pop_up_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.delete_note -> {
                        listener.onLongItemClicked(currentNote, holder.notes_layout)
                        true
                    }
                    R.id.set_alarm -> {
                        alarmListener.onAlarmClicked(currentNote)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
            true
        }
    }


    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val notes_layout = itemView.findViewById<CardView>(R.id.card_layout)
        val title = itemView.findViewById<TextView>(R.id.tv_title)
        val note_tv = itemView.findViewById<TextView>(R.id.tv_note)
        val date = itemView.findViewById<TextView>(R.id.tv_date)
    }

    interface NotesClickListener {
        fun onItemClicked(note: Note)
        fun onLongItemClicked(note: Note, cardView: CardView)
    }

    // NUEVA INTERFACE PARA LA ALARMA
    interface AlarmClickListener {
        fun onAlarmClicked(note: Note)
    }
}
