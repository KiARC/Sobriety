package com.katiearose.sobriety.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.katiearose.sobriety.Addiction
import com.katiearose.sobriety.R
import com.katiearose.sobriety.TimelineAdapter
import com.katiearose.sobriety.databinding.ActivityTimelineBinding
import com.katiearose.sobriety.utils.applyThemes

class Timeline : AppCompatActivity() {

    private lateinit var binding: ActivityTimelineBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemes()
        super.onCreate(savedInstanceState)
        binding = ActivityTimelineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val addictionPosition = intent.extras!!.getSerializable(Main.EXTRA_ADDICTION_POSITION) as Int
        val addiction = Main.addictions[addictionPosition]

        binding.timelineNotice.text = getString(R.string.showing_timeline, addiction.name)
        val adapter = TimelineAdapter(addiction, this)
        binding.timelineList.layoutManager = LinearLayoutManager(this)
        binding.timelineList.setHasFixedSize(true)
        binding.timelineList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.timelineList.adapter = adapter
    }
}