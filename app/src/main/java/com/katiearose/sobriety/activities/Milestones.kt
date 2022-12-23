package com.katiearose.sobriety.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.katiearose.sobriety.Addiction
import com.katiearose.sobriety.MilestoneAdapter
import com.katiearose.sobriety.R
import com.katiearose.sobriety.databinding.ActivityMilestonesBinding
import com.katiearose.sobriety.databinding.DialogAddMilestoneBinding
import com.katiearose.sobriety.internal.CacheHandler
import com.katiearose.sobriety.utils.applyThemes
import com.katiearose.sobriety.utils.isInputEmpty
import com.katiearose.sobriety.utils.showConfirmDialog
import java.time.temporal.ChronoUnit

class Milestones : AppCompatActivity() {

    private lateinit var binding: ActivityMilestonesBinding
    private lateinit var addiction: Addiction
    private lateinit var cacheHandler: CacheHandler
    private lateinit var adapter: MilestoneAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        applyThemes()
        super.onCreate(savedInstanceState)
        binding = ActivityMilestonesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cacheHandler = CacheHandler(this)

        val addictionPosition = intent.extras!!.getSerializable(Main.EXTRA_ADDICTION_POSITION) as Int
        addiction = Main.addictions[addictionPosition]

        adapter = MilestoneAdapter(addiction, this) {
            val action: () -> Unit = {
                addiction.milestones.remove(it)
                cacheHandler.writeCache()
                update()
            }
            showConfirmDialog(getString(R.string.delete), getString(R.string.delete_milestone_confirm), action)
        }
        binding.milestoneList.layoutManager = LinearLayoutManager(this)
        binding.milestoneList.setHasFixedSize(true)
        binding.milestoneList.adapter = adapter

        binding.addMilestoneFab.setOnClickListener {
            var dialogViewBinding: DialogAddMilestoneBinding? =
                DialogAddMilestoneBinding.inflate(layoutInflater)
            val dialog = BottomSheetDialog(this)
            dialog.setContentView(dialogViewBinding!!.root)
            val units = resources.getStringArray(R.array.time_units)
            dialogViewBinding.btnSaveMilestone.setOnClickListener {
                if (dialogViewBinding!!.milestoneNumberInput.isInputEmpty() ||
                    dialogViewBinding!!.milestoneNumberInput.text.toString().toInt() == 0) {
                    dialogViewBinding!!.milestoneNumberInputLayout.error =
                        getString(R.string.error_empty_amount)
                } else if (dialogViewBinding!!.unitInput.text.toString().isEmpty()) {
                    dialogViewBinding!!.milestoneTimeUnitInputLayout.error =
                        getString(R.string.error_empty_unit)
                } else {
                    val num = dialogViewBinding!!.milestoneNumberInput.text.toString().toInt()
                    when (dialogViewBinding!!.unitInput.text.toString()) {
                        units[0] -> addiction.milestones.add(Pair(num, ChronoUnit.HOURS))
                        units[1] -> addiction.milestones.add(Pair(num, ChronoUnit.DAYS))
                        units[2] -> addiction.milestones.add(Pair(num, ChronoUnit.WEEKS))
                        units[3] -> addiction.milestones.add(Pair(num, ChronoUnit.MONTHS))
                        units[4] -> addiction.milestones.add(Pair(num, ChronoUnit.YEARS))
                    }
                    cacheHandler.writeCache()
                    adapter.update()
                    dialog.dismiss()
                }
            }
            dialog.setOnDismissListener { dialogViewBinding = null }
            dialog.show()
        }
    }

    private fun update() {
        cacheHandler.writeCache()
        adapter.update()
    }
}