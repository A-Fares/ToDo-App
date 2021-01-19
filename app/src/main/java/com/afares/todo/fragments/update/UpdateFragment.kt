package com.afares.todo.fragments.update

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afares.todo.R
import com.afares.todo.data.model.ToDoData
import com.afares.todo.data.viewmodel.ToDoViewModel
import com.afares.todo.databinding.FragmentUpdateBinding
import com.afares.todo.fragments.SharedViewModel
import kotlinx.android.synthetic.main.fragment_update.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UpdateFragment : Fragment() {

    private val args: UpdateFragmentArgs by navArgs()

    private val mSharedViewModel: SharedViewModel by viewModels()
    private val mToDoViewModel: ToDoViewModel by viewModels()

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /// Data Binding
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)

        // Set Menu
        setHasOptionsMenu(true)
        binding.apply {
            currentTitleEt.setText(args.currentItem.title)
            currentDescriptionEt.setText(args.currentItem.description)
            currentPrioritiesSpinner.setSelection(mSharedViewModel.parsePriorityToInt(args.currentItem.priority))
            currentPrioritiesSpinner.onItemSelectedListener = mSharedViewModel.listener
        }


        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> updateItem()
            R.id.menu_delete -> confirmItemRemoval()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateItem() {
        val title = current_title_et.text.toString()
        val description = current_description_et.text.toString()
        val getPriority = current_priorities_spinner.selectedItem.toString()

        val validation = mSharedViewModel.verifyDataFromUser(title, description)
        if (validation) {
            val updatedItem = ToDoData(
                args.currentItem.id,
                title,
                mSharedViewModel.parsePriority(getPriority),
                description
            )
            mToDoViewModel.updateData(updatedItem)
            showToast("Successfully updated!")
            //Navigate to back
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        } else {
            showToast("Please fill out all fields.")
        }
    }

    //Show AlertDialog to Confirm item Removal
    private fun confirmItemRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            mToDoViewModel.deleteItem(args.currentItem)
            showToast("Successfully removed: ${args.currentItem.title}")
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete '${args.currentItem.title}'")
        builder.setMessage("Are you sure you want to remove '${args.currentItem.title}'?")
        builder.create().show()
    }

    private fun showToast(text: String) {
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // to avoid memory leaks
        _binding = null
    }
}