package com.example.edilei.data.model

enum class CategoriaCusto(
    val titulo: String,
    val descricao: String,
    val ordem: Int
) {
    ARREMATACAO(
        titulo = "1. Arrematação & Entrada",
        descricao = "Lance, comissão do leiloeiro (5%), taxas bancárias da Caixa",
        ordem = 1
    ),
    DEBITOS_ANTERIORES(
        titulo = "2. Dívidas Anteriores (Passivo)",
        descricao = "IPTU atrasado, condomínio acumulado e contas de consumo anteriores",
        ordem = 2
    ),
    JURIDICO_DESOCUPACAO(
        titulo = "3. Jurídico & Desocupação",
        descricao = "Honorários advocatícios, imissão na posse e acordo de desocupação amigável",
        ordem = 3
    ),
    CARTORIO_IMPOSTOS(
        titulo = "4. Cartório, ITBI & Matrícula",
        descricao = "ITBI, certidões, carta de arrematação, escritura e registro de imóveis",
        ordem = 4
    ),
    MANUTENCAO_REFORMA(
        titulo = "5. Manutenção, Reforma & Contas",
        descricao = "Pintura, reforma, ligação/contas de água, luz, gás, licenças e condomínio vigente",
        ordem = 5
    ),
    VENDA_DESINVESTIMENTO(
        titulo = "6. Venda",
        descricao = "Comissão do corretor de imóveis (6%) e imposto de ganho de capital",
        ordem = 6
    )
}
