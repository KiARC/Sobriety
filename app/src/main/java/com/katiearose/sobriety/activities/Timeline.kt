package com.katiearose.sobriety.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.katiearose.sobriety.R
import com.katiearose.sobriety.TimelineAdapter
import com.katiearose.sobriety.TimelineAdapterAlt
import com.katiearose.sobriety.databinding.ActivityTimelineBinding
import com.katiearose.sobriety.shared.Addiction
import com.katiearose.sobriety.utils.applyThemes
import com.katiearose.sobriety.utils.checkValidIntentData
import com.katiearose.sobriety.utils.convertSecondsToString
import com.katiearose.sobriety.utils.getAltTimelinePref
import com.katiearose.sobriety.utils.getSharedPref

class Timeline : AppCompatActivity() {

    private lateinit var binding: ActivityTimelineBinding
    private lateinit var addiction: Addiction

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemes()
        super.onCreate(savedInstanceState)
        binding = ActivityTimelineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val useAltTimelinePref = getSharedPref().getAltTimelinePref()
        addiction = Main.addictions[checkValidIntentData()]
        binding.timelineNotice.text = getString(R.string.showing_timeline, addiction.name)
        binding.timelineList.layoutManager = LinearLayoutManager(this)
        binding.timelineList.setHasFixedSize(true)
        if (!useAltTimelinePref)
            binding.timelineList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        binding.timelineList.adapter = if (useAltTimelinePref) TimelineAdapterAlt(addiction)
        else TimelineAdapter(addiction, this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_timeline, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        fun buildNonZeroIndicesAsString(list: List<Int>): StringBuilder = StringBuilder().apply {
            list.forEachIndexed { index, i ->
                if (index > 0) {
                    append(", ${i + 1}")
                }
            }
        }

        val id = item.itemId
        if (id == R.id.calc_avg_duration) {
            if (addiction.history.size == 1 || (addiction.history.size == 2 && addiction.history.toList()[1].second == 0L)) {
                Toast.makeText(this, R.string.only_one_attempt, Toast.LENGTH_SHORT).show()
            } else {
                val checked = BooleanArray(addiction.history.size - 1)
                val items = List(checked.size) { index -> getString(R.string.attempt, index + 1) }.toTypedArray()
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.calc_avg_durations)
                    .setMultiChoiceItems(items, checked) { _, which, isChecked -> checked[which] = isChecked }
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val indices = checked.mapIndexed { index, b ->
                            if (b) index else -1
                        }.filter { it != -1 }
                        when {
                            indices.isEmpty() -> Toast.makeText(this, R.string.nothing_is_chosen, Toast.LENGTH_SHORT).show()
                            indices.size == 1 -> Toast.makeText(this, R.string.only_one_attempt_is_chosen, Toast.LENGTH_SHORT).show()
                            else -> binding.avgDuration.text = StringBuilder(getString(R.string.avg_duration))
                                .append(" ${indices[0] + 1}")
                                .append(buildNonZeroIndicesAsString(indices))
                                .append(": ${convertSecondsToString(addiction.calculateAvgRelapseDuration(indices) / 1000L)}")
                        }
                    }
                    .show()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}