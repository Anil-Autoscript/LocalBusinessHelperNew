package com.localbusiness.helper.ui.settings

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.localbusiness.helper.data.repository.BusinessRepository
import com.localbusiness.helper.data.repository.Result
import com.localbusiness.helper.databinding.FragmentSettingsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSavedSettings()
        setupClickListeners()
    }

    private fun loadSavedSettings() {
        binding.etSheetsId.setText(prefs.getString("sheets_id", ""))
        binding.etApiKey.setText(prefs.getString("sheets_api_key", ""))
        binding.etRange.setText(prefs.getString("sheets_range", "Sheet1!A2:I"))
    }

    private fun setupClickListeners() {
        binding.btnSaveSettings.setOnClickListener {
            val sheetsId = binding.etSheetsId.text?.toString()?.trim() ?: ""
            val apiKey = binding.etApiKey.text?.toString()?.trim() ?: ""
            val range = binding.etRange.text?.toString()?.trim() ?: "Sheet1!A2:I"

            prefs.edit()
                .putString("sheets_id", sheetsId)
                .putString("sheets_api_key", apiKey)
                .putString("sheets_range", range.ifEmpty { "Sheet1!A2:I" })
                .apply()

            Snackbar.make(binding.root, "Settings saved!", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnTestSync.setOnClickListener {
            binding.btnTestSync.isEnabled = false
            binding.btnTestSync.text = "Testing..."

            lifecycleScope.launch {
                val repo = BusinessRepository(requireContext())
                when (val result = withContext(Dispatchers.IO) { repo.syncFromGoogleSheets() }) {
                    is Result.Success -> {
                        Snackbar.make(binding.root, "✓ Sync successful! ${result.data} records.", Snackbar.LENGTH_LONG).show()
                    }
                    is Result.Error -> {
                        Snackbar.make(binding.root, "✗ ${result.message}", Snackbar.LENGTH_LONG).show()
                    }
                    else -> {}
                }
                binding.btnTestSync.isEnabled = true
                binding.btnTestSync.text = "Test Connection"
            }
        }

        binding.tvHowTo.setOnClickListener {
            binding.layoutInstructions.visibility =
                if (binding.layoutInstructions.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
