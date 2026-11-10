package com.example.edilei.data.model

data class AporteSocio(
    val socio: Socio,
    val totalPago: Double,
    val percentualDoTotal: Double,
    val lucroProjetadoSocio: Double
)

data class ResumoFinanceiro(
    val imovelId: String,
    val totalEstimado: Double,
    val totalEfetivado: Double,
    val custoTotalAtual: Double,
    val aportesPorSocio: List<AporteSocio>,
    val lucroProjetado: Double,
    val roiProjetadoPercentual: Double,
    val lucroRealizado: Double? = null
)
