package locidnet.com.marvarid.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import locidnet.com.marvarid.R

import locidnet.com.marvarid.connectors.AdapterClicker
import locidnet.com.marvarid.model.Color
import kotlin.properties.Delegates


class ColorPaletteAdapter(clicker:AdapterClicker, ctx:Context, map:HashMap<Int,Color>) : RecyclerView.Adapter<ColorPaletteAdapter.Adapter>() {
    val adapterClicker = clicker
    val context = ctx
    val colors = map
    val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun onBindViewHolder(p0: Adapter?, p1: Int) {
        val color = colors.get(p1)!!.drawable

        p0!!.view.setBackgroundColor(ContextCompat.getColor(context,color))
        p0.view.setOnClickListener { adapterClicker.click(p1) }
    }

    override fun getItemCount(): Int = colors.size

    override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): Adapter =
            Adapter(inflater.inflate(R.layout.res_color_palette_list_item,p0,false))

    class Adapter(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var view by Delegates.notNull<View>()
        init {
            view = itemView.findViewById(R.id.color)

        }
    }
}