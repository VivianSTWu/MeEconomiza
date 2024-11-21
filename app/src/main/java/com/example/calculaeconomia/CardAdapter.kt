package com.example.calculaeconomia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CardAdapter(private val cardList: List<CardInfo>) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNomeEndereco: TextView = itemView.findViewById(R.id.textViewApelido)
        val txtEnergiaSolar: TextView = itemView.findViewById(R.id.textViewSolar)
        val txtEnergiaEolica: TextView = itemView.findViewById(R.id.textViewEolica)
        val txtEconomia: TextView = itemView.findViewById(R.id.textViewEconomia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_resultados, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val cardInfo = cardList[position]
        holder.txtNomeEndereco.text = cardInfo.nomeEndereco
        holder.txtEnergiaSolar.text = "Energia Solar: ${cardInfo.energiaSolarGerada} kWh"
        holder.txtEnergiaEolica.text = "Energia Eólica: ${cardInfo.energiaEolicaGerada} kWh"
        holder.txtEconomia.text = "Economia: R$ ${cardInfo.economia}"
    }

    override fun getItemCount(): Int = cardList.size
}
