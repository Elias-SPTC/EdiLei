package com.example.edilei.data.model

data class Custo(
    val id: String,
    val imovelId: String,
    val titulo: String,
    val categoria: CategoriaCusto,
    val valorEstimado: Double,
    val valorEfetivado: Double? = null,
    val status: StatusCusto = StatusCusto.PENDENTE,
    val efetuadoPorSocioId: String? = null,
    val efetuadoPorSocioNome: String? = null,
    val dataVencimento: Long? = null,
    val dataEfetivacao: Long? = null,
    val observacoes: String = ""
) {
    val valorFinal: Double
        get() = if (status == StatusCusto.EFETIVADO && valorEfetivado != null) valorEfetivado else valorEstimado
}
