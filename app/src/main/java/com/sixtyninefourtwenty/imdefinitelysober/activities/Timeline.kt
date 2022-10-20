package com.sixtyninefourtwenty.imdefinitelysober.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sixtyninefourtwenty.imdefinitelysober.R
import com.sixtyninefourtwenty.imdefinitelysober.TimelineAdapter
import com.sixtyninefourtwenty.imdefinitelysober.databinding.ActivityTimelineBinding

class Timeline : AppCompatActivity() {

    private lateinit var binding: ActivityTimelineBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimelineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pos = intent.extras!!.getInt(Main.EXTRA_ADDICTION_POSITION)
        val addiction = Main.addictions[pos]
        binding.timelineNotice.text = getString(R.string.showing_timeline, addiction.name)
        val adapter = TimelineAdapter(this)
        adapter.setHistory(addiction.history)
        binding.timelineList.layoutManager = LinearLayoutManager(this)
        binding.timelineList.setHasFixedSize(true)
        binding.timelineList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.timelineList.adapter = adapter
    }
}