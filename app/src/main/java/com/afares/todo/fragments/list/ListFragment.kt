package com.afares.todo.fragments.list

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.afares.todo.R
import com.afares.todo.data.model.ToDoData
import com.afares.todo.data.viewmodel.ToDoViewModel
import com.afares.todo.databinding.FragmentListBinding
import com.afares.todo.fragments.SharedViewModel
import com.afares.todo.fragments.list.adapter.ListAdapter
import com.afares.todo.utils.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val mSharedViewModel: SharedViewModel by viewModels()
    private val mToDoViewModel: ToDoViewModel by viewModels()

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val adapter: ListAdapter by lazy { ListAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Data Binding
        _binding = FragmentListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.mSharedViewModel = mSharedViewModel

        // Init RecyclerView
        initRecyclerView()

        // Observe LiveData
        mToDoViewModel.getAllData.observe(viewLifecycleOwner, { data ->
            mSharedViewModel.cechIfDatabaseEmpty(data)
            adapter.setData(data)
        })

        // Set Menu
        setHasOptionsMenu(true)

        // Hide soft Keyboard
        hideKeyboard(requireActivity())

        return binding.root
    }

    private fun initRecyclerView() {
        val recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.itemAnimator = SlideInUpAnimator().apply {
            addDuration = 300
        }
        // Swipe to delete
        swipeToDelete(recyclerView)
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem = adapter.dataList[viewHolder.adapterPosition]
                // Delete Item
                mToDoViewModel.deleteItem(deletedItem)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
                showToast("Successfully Removed: '${deletedItem.title}'")
                // Restore Deleted Item
                restoreDeletedData(viewHolder.itemView, deletedItem)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreDeletedData(view: View, deletedItem: ToDoData) {
        val snackbar = Snackbar.make(
            view, "Deleted '${deletedItem.title}'",
            Snackbar.LENGTH_LONG
        )
        snackbar.setAction("Undo") {
            mToDoViewModel.insertData(deletedItem)
        }
        snackbar.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)

        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_delete_all -> confirmRemoval()
            R.id.menu_priority_high -> mToDoViewModel.sortByHighPriority.observe(this,
                { adapter.setData(it) })
            R.id.menu_priority_low -> mToDoViewModel.sortByLowPriority.observe(this,
                { adapter.setData(it) })
        }
        return super.onOptionsItemSelected(item)
    }

    //Show AlertDialog to Confirm Removal EveryThing
    private fun confirmRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            mToDoViewModel.deleteAll()
            showToast("Successfully removed Everything")
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete Everything!")
        builder.setMessage("Are you sure you want to remove Everything?")
        builder.create().show()
    }

    private fun showToast(text: String) {
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // To Avoid Memory Leaks
        _binding = null
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }

    private fun searchThroughDatabase(query: String) {
        val searchQuery = "%$query%"

        mToDoViewModel.searchDatabase(searchQuery).observe(this, { list ->
            list?.let {
                adapter.setData(it)
            }
        })
    }
}