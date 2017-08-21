package me.niccorder.prime.sieve.view

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import me.niccorder.prime.R


class PrimeNumberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val number: TextView = itemView.findViewById(R.id.number_tv)

    companion object {
        fun create(context: Context): PrimeNumberViewHolder = PrimeNumberViewHolder(
                View.inflate(
                        context,
                        R.layout.item_prime_number,
                        null
                )
        )
    }
}

class PrimeAdapter(
        private val countFunc: () -> Int,
        private val primeAt: (position: Int) -> Long
) : RecyclerView.Adapter<PrimeNumberViewHolder>() {

    private var displayedItemCount: Int = countFunc.invoke()
    private var animateFirstItem = false

    override fun onCreateViewHolder(
            parent: ViewGroup?,
            viewType: Int
    ): PrimeNumberViewHolder = PrimeNumberViewHolder.create(parent!!.context)

    override fun getItemCount(): Int = displayedItemCount

    override fun onBindViewHolder(holder: PrimeNumberViewHolder?, position: Int) {
        val adjustedPosition = displayedItemCount - position - 1
        holder!!.number.text = primeAt.invoke(adjustedPosition).toString()

        animateItemAdded(position, holder.number)
    }

    private fun animateItemAdded(position: Int, textView: TextView) {
        ContextCompat.getColor(textView.context, colors[position % colors.size])
        val animation = ValueAnimator.ofObject(
                ArgbEvaluator(),
                ContextCompat.getColor(textView.context, colors[displayedItemCount % colors.size]),
                ContextCompat.getColor(textView.context, R.color.black_70)
        )

        animation.duration = 2000L
        animation.addUpdateListener { textView.setTextColor(it.animatedValue as Int) }
        animation.start()
    }

    fun notifyPrimeAdded() {
        animateFirstItem = true
        displayedItemCount++
        notifyItemInserted(0)
    }

    fun clearAdapter() {
        displayedItemCount = 0
        notifyDataSetChanged()
    }

    companion object {
        const val ANIMATION_DURATION = 2000L

        private val colors = arrayOf(
                R.color.yellow_brown,
                R.color.red,
                R.color.colorAccent,
                R.color.green,
                R.color.pink,
                R.color.light_sea_green
        )

    }
}