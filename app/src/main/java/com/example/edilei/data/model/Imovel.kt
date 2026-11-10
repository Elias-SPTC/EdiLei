package com.example.edilei.data.model

data class Imovel(
    val id: String,
    val titulo: String,
    val endereco: String = "",
    val rua: String = "",
    val numero: String = "",
    val bairro: String = "",
    val cidade: String = "",
    val uf: String = "MG",
    val condominio: String = "",
    val bloco: String = "",
    val apartamento: String = "",
    val bancoOrigem: String = "CAIXA",
    val status: StatusImovel = StatusImovel.ARREMATADO,
    val valorAvaliacao: Double,
    val valorMatricula: Double = 0.0,
    val valorLanceArrematado: Double,
    val valorVendaPretendido: Double,
    val valorVendaRealizado: Double? = null,
    val criadoEm: Long = System.currentTimeMillis()
) {
    val enderecoCompleto: String
        get() {
            if (rua.isBlank()) return endereco.ifBlank { "Endereço não informado" }
            val partes = mutableListOf<String>()
            val logradouro = if (numero.isNotBlank()) "$rua, $numero" else rua
            partes.add(logradouro)

            val complementos = mutableListOf<String>()
            if (apartamento.isNotBlank()) complementos.add("Apt $apartamento")
            if (bloco.isNotBlank()) complementos.add("Bloco $bloco")
            if (condominio.isNotBlank()) complementos.add(condominio)
            if (complementos.isNotEmpty()) partes.add(complementos.joinToString(", "))

            if (bairro.isNotBlank()) partes.add(bairro)
            if (cidade.isNotBlank()) {
                val loc = if (uf.isNotBlank()) "$cidade/$uf" else cidade
                partes.add(loc)
            }
            return partes.joinToString(" - ")
        }
}
