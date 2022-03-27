package com.katiearose.sobriety

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.time.Instant
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream


class Main : AppCompatActivity() {

    companion object {
        const val EXTRA_NAMES = "com.katiearose.sobriety.EXTRA_NAMES"
        fun secondsToString(given: Long): String {
            var time = given
            val s = time % 60
            time -= s
            val m = (time % 3600) / 60
            time -= m * 60
            val h = (time % 86400) / 3600
            time -= h * 3600
            val d = time / 86400
            return "$d days, $h hours, $m minutes and $s seconds"
        }

        fun timeSinceInstant(given: Instant) = Instant.now().epochSecond - given.epochSecond

        val addictions = ArrayList<Addiction>()
    }

    private lateinit var addCardButton: FloatingActionButton
    private lateinit var cardHolder: LinearLayout
    private lateinit var prompt: TextView

    private lateinit var adapterAddictions: AddictionCardAdapter

    private val createCardRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        addCardButton = findViewById(R.id.addCardButton)
        addCardButton.setOnClickListener { newCardDialog() }
//        cardHolder = findViewById(R.id.container)
        prompt = findViewById(R.id.prompt)
        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(16, 16, 16, 16)
        try {
            this.openFileInput("Sobriety.cache").use {
                readCache(it)
            }
        } catch (e: FileNotFoundException) {
        }
        updatePromptVisibility()

        adapterAddictions = AddictionCardAdapter(this)
        val recyclerAddictions = findViewById<RecyclerView>(R.id.recyclerAddictions)
        val layoutManager = LinearLayoutManager(this)
        recyclerAddictions.layoutManager = layoutManager
        recyclerAddictions.adapter = adapterAddictions



    }

    fun updatePromptVisibility() {
        prompt.visibility = when (addictions.size == 0) {
            true -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun newCardDialog() {
        //Pass current addiction names to create activity, to prevent creation of elements with identical keys
        val addictionNames = arrayListOf<String>()
        addictions.forEach {
            addictionNames.add(it.name)
        }
        val intent = Intent(this, Create::class.java)
        intent.putStringArrayListExtra(EXTRA_NAMES, addictionNames)
        startActivityForResult(intent, createCardRequestCode)
    }

    fun dialogConfirm(title: String, confirmAction: () -> Unit) {
        this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(
                    "ok"
                ) { _, _ ->
                    confirmAction()
                }
                setNegativeButton("cancel") { _: DialogInterface, _: Int -> }
            }
            builder.setTitle(title)
            builder.create()
            builder.show()
        }
    }

    private fun readCache(input: InputStream) {
        val cache = input.readBytes()
        try {
            InflaterInputStream(cache.inputStream()).use { iis ->
                ObjectInputStream(iis).use {
                    addictions.addAll(it.readObject() as ArrayList<Addiction>)
                }
            }
        } catch (e: ClassCastException) {
            readCache(cache.inputStream())
        }
    }

    @Deprecated(
        "For old caches before the implementation of the Addiction class.",
        ReplaceWith("readCache(FileInputStream)"),
        DeprecationLevel.WARNING
    )
    private fun readLegacyCache(input: InputStream) {
        val a = HashMap<String, Pair<Instant, CircularBuffer<Long>>>()
        InflaterInputStream(input).use { iis ->
            ObjectInputStream(iis).use {
                for (i in it.readObject() as HashMap<String, Pair<Instant, CircularBuffer<Long>>>) {
                    a[i.key] = i.value
                }
            }
        }
        for (ad in a) {
            val addiction = Addiction(ad.key, ad.value.first)
        }
    }

    private fun writeCache() {
        this.openFileOutput("Sobriety.cache", MODE_PRIVATE).use { fos ->
            DeflaterOutputStream(fos, true).use { dos ->
                ObjectOutputStream(dos).use {
                    it.writeObject(addictions)
                }
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        writeCache()
    }


    /**
     * This gets called once the Create Activity is closed (Necessary to hide the prompt in case
     * a first addiction was added to the list.
     */
    override fun onResume() {
        updatePromptVisibility()
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == createCardRequestCode) {
            if (resultCode == RESULT_OK) {
                val name = data?.extras?.get("name") as String
                val instant = data.extras?.get("instant") as Instant
                val addiction = Addiction(name, instant)
                addictions.add(addiction)
                adapterAddictions.notifyDataSetChanged()
            }
        }
    }
}

class AddictionCardAdapter(val activity: Main): RecyclerView.Adapter<AddictionCardAdapter.AddictionCardViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AddictionCardViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.card_addiction, parent, false)
        return AddictionCardViewHolder(itemView)
    }

    override fun onBindViewHolder(
        holder: AddictionCardViewHolder,
        position: Int
    ) {
        val addiction = Main.addictions[position]

        holder.textViewName.text = addiction.name
        holder.textViewTime.text = Main.secondsToString(Main.timeSinceInstant(addiction.lastRelapse))
        holder.textViewAverage.text = "Average: ${Main.secondsToString(addiction.averageRelapseDuration)}"

        holder.buttonDelete.setOnClickListener {
            val action: () -> Unit = {
                Main.addictions.remove(addiction)
                activity.updatePromptVisibility()
                notifyItemRemoved(position)
            }
            activity.dialogConfirm("Delete entry \"${addiction.name}\" ?", action)
        }

        holder.buttonReset.setOnClickListener {
            val action: () -> Unit = {
                addiction.relapse()
                notifyItemChanged(position)
            }
            activity.dialogConfirm("Reset entry \"${addiction.name}\" ?", action)
        }

        holder.cardView.setOnClickListener {
            notifyItemChanged(position)
        }

        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.postDelayed(object : Runnable {
            override fun run() {
                if (!Main.addictions.contains(addiction)) {
                    return
                }
                holder.textViewTime.text =
                    Main.secondsToString(Main.timeSinceInstant(addiction.lastRelapse))
                mainHandler.postDelayed(this, 1000L)
            }
        }, 1000L)
    }

    override fun getItemCount() = Main.addictions.size

    class AddictionCardViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val textViewName: TextView = itemView.findViewById(R.id.textViewAddictionName)
        val textViewTime: TextView = itemView.findViewById(R.id.textViewTime)
        val textViewAverage: TextView = itemView.findViewById(R.id.textViewAverage)
        val buttonDelete: ImageView = itemView.findViewById(R.id.imageDelete)
        val buttonReset: ImageView = itemView.findViewById(R.id.imageReset)
        val cardView: CardView = itemView.findViewById(R.id.cardViewAddiction)
    }
}
