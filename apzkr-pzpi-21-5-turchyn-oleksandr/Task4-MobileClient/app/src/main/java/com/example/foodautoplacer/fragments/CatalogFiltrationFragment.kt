package com.example.foodautoplacer.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import com.example.foodautoplacer.R
import com.example.foodautoplacer.activities.catalog.DishCatalog
import kotlinx.android.synthetic.main.fragment_alert_dialog.view.*
import kotlinx.android.synthetic.main.fragment_unfolding_item.view.*
import kotlinx.android.synthetic.main.fragment_unfolding_rating_interval.view.*


class CatalogFiltrationFragment(catalog: DishCatalog) : DialogFragment() {
    private val types: MutableSet<String> = catalog.types
    private val popularities: MutableSet<String> = catalog.popularities
    private val chosenTypes: MutableSet<String> = catalog.chosenTypes
    private val chosenPopularities: MutableSet<String> = catalog.chosenPopularities
    private val catalogReference = catalog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

//            chosenCategories.clear()

            val inflater = requireActivity().layoutInflater
            val canvasView = inflater.inflate(R.layout.fragment_alert_dialog, null)
            val unfoldingItems: LinearLayout = canvasView.filterItem
            val unfoldingItemInflater: LayoutInflater = LayoutInflater.from(context)
            var expandableButton: Button
            var expandableCharacteristicsView: ConstraintLayout
            var expandableCard: CardView

            val typeView = unfoldingItemInflater.inflate(
                R.layout.fragment_unfolding_item, unfoldingItems, false)
            addCheckBoxesToView(typeView, types, chosenTypes)
            typeView.unfolding_title.text = resources.getString(R.string.catalog_filter_type)
            unfoldingItems.addView(typeView)

            expandableButton = typeView.unfolding_item_characteristics_arrow_button
            expandableCharacteristicsView = typeView.unfolding_item_expandable_constraint
            expandableCard = typeView.expandable_card
            setListeners(expandableButton, expandableCharacteristicsView, expandableCard)

            val popularityView = unfoldingItemInflater.inflate(
                R.layout.fragment_unfolding_item, unfoldingItems, false)
            popularityView.unfolding_title.text = resources.getString(R.string.catalog_filter_popularity)
            addCheckBoxesToView(popularityView, popularities, chosenPopularities)
            unfoldingItems.addView(popularityView)

            expandableButton = popularityView.unfolding_item_characteristics_arrow_button
            expandableCharacteristicsView = popularityView.unfolding_item_expandable_constraint
            expandableCard = popularityView.expandable_card
            setListeners(expandableButton, expandableCharacteristicsView, expandableCard)

            val ratingView = unfoldingItemInflater.inflate(
                R.layout.fragment_unfolding_rating_interval,
                unfoldingItems, false)
            ratingView.unfolding_rating_item_characteristics_arrow_button.text = resources.getString(R.string.catalog_rating)
            ratingView.editTextNumberSigned5.setText(catalogReference.minRate.toString())
            ratingView.editTextNumberSigned4.setText(catalogReference.maxRate.toString())
            unfoldingItems.addView(ratingView)
            expandableButton = ratingView.unfolding_rating_arrow_button
            expandableCharacteristicsView = ratingView.unfolding_rating_expandable_constraint
            expandableCard = ratingView.expandable_rating_card
            setListeners(expandableButton, expandableCharacteristicsView, expandableCard)

            builder.setTitle(resources.getString(R.string.catalog_filtration))
                .setView(canvasView)
                .setPositiveButton(resources.getString(R.string.confirm),
                    DialogInterface.OnClickListener { _, _ ->
                        catalogReference.minRate = ratingView.editTextNumberSigned5
                            .text.toString().toFloat()
                        catalogReference.maxRate = ratingView.editTextNumberSigned4
                            .text.toString().toFloat()
                        catalogReference.fillAppropriateDishContainer()
                    })
            val alert = builder.create()
            alert.setOnShowListener {
                val btnPositive = alert.getButton(Dialog.BUTTON_POSITIVE)
                btnPositive.textSize = 15F
                val btnNegative = alert.getButton(Dialog.BUTTON_NEGATIVE)
                btnNegative.textSize = 14F
            }

            return alert
        } ?: throw IllegalStateException("Activity cannot be null")
    }



    private fun addCheckBoxesToView(view: View,
                                    itemSet: MutableSet<String>,
                                    filterParamsSet: MutableSet<String>) {
        for (i in itemSet) {
            val currentCheckBox: CheckBox = CheckBox(context)
            currentCheckBox.text = i
            currentCheckBox.textSize = 17F
            if (i in filterParamsSet) {
                currentCheckBox.isChecked = true
            }

            currentCheckBox.setOnClickListener {
                if (currentCheckBox.isChecked) {
                    filterParamsSet.add(currentCheckBox.text.toString())
                } else {
                    filterParamsSet.remove(currentCheckBox.text.toString())
                }
            }
            view.unfoldingItemListLayout.addView(currentCheckBox)
        }
    }

    private fun setListeners(button: Button, expandedView: ConstraintLayout, card: CardView) {
        button.setOnClickListener {
            if (expandedView.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(
                    card, AutoTransition()
                )
                expandedView.visibility = View.VISIBLE
                button.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
            } else {
                TransitionManager.beginDelayedTransition(
                    card, AutoTransition()
                )
                expandedView.visibility = View.GONE
                button.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            }
        }
    }
}