package com.example.edilei.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.edilei.data.model.CategoriaCusto
import com.example.edilei.data.model.Custo
import com.example.edilei.data.model.Imovel
import com.example.edilei.data.model.ResumoFinanceiro
import com.example.edilei.data.model.Socio
import com.example.edilei.data.model.StatusCusto
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImovelDetailScreen(
    imovel: Imovel,
    custos: List<Custo>,
    resumoFinanceiro: ResumoFinanceiro?,
    socios: List<Socio>,
    socioAtualId: String,
    onBack: () -> Unit,
    onEditarImovel: (Imovel) -> Unit,
    onEfetivarCusto: (custoId: String, valorEfetivado: Double, socioId: String, observacoes: String) -> Unit,
    onAdicionarCusto: (titulo: String, categoria: CategoriaCusto, valorEstimado: Double) -> Unit,
    onRemoverCusto: (custoId: String) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var custoParaEfetivar by remember { mutableStateOf<Custo?>(null) }
    var showAddCustoDialog by remember { mutableStateOf(false) }
    var showEditImovelDialog by remember { mutableStateOf(false) }

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = imovel.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${imovel.bancoOrigem} • ${imovel.status.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showEditImovelDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Imóvel",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 1) {
                FloatingActionButton(
                    onClick = { showAddCustoDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Custo")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Resumo & Balanço", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Checklist de Custos (${custos.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            when (selectedTabIndex) {
                0 -> {
                    ResumoTabContent(
                        imovel = imovel,
                        resumo = resumoFinanceiro,
                        currencyFormatter = currencyFormatter
                    )
                }

                1 -> {
                    CustosTabContent(
                        custos = custos,
                        currencyFormatter = currencyFormatter,
                        onEfetivarClick = { custoParaEfetivar = it },
                        onRemoverClick = { onRemoverCusto(it) }
                    )
                }
            }
        }

        custoParaEfetivar?.let { custo ->
            EfetivarCustoDialog(
                custo = custo,
                socios = socios,
                socioAtualId = socioAtualId,
                onDismiss = { custoParaEfetivar = null },
                onConfirm = { valor, socioId, obs ->
                    onEfetivarCusto(custo.id, valor, socioId, obs)
                    custoParaEfetivar = null
                }
            )
        }

        if (showAddCustoDialog) {
            AddCustoDialog(
                onDismiss = { showAddCustoDialog = false },
                onConfirm = { titulo, categoria, valor ->
                    onAdicionarCusto(titulo, categoria, valor)
                    showAddCustoDialog = false
                }
            )
        }

        if (showEditImovelDialog) {
            EditarImovelDialog(
                imovel = imovel,
                onDismiss = { showEditImovelDialog = false },
                onConfirm = { imovelEditado ->
                    onEditarImovel(imovelEditado)
                    showEditImovelDialog = false
                }
            )
        }
    }
}

@Composable
fun ResumoTabContent(
    imovel: Imovel,
    resumo: ResumoFinanceiro?,
    currencyFormatter: NumberFormat
) {
    if (resumo == null) return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Projeção Financeira do Imóvel",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Valor de Avaliação", style = MaterialTheme.typography.bodySmall)
                            Text(
                                currencyFormatter.format(imovel.valorAvaliacao),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column {
                            Text("Valor de Matrícula", style = MaterialTheme.typography.bodySmall)
                            Text(
                                currencyFormatter.format(imovel.valorMatricula),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Lance Arrematado", style = MaterialTheme.typography.bodySmall)
                            Text(
                                currencyFormatter.format(imovel.valorLanceArrematado),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text("Venda Pretendida", style = MaterialTheme.typography.bodySmall)
                            Text(
                                currencyFormatter.format(imovel.valorVendaPretendido),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Custo Total Atual", style = MaterialTheme.typography.bodySmall)
                            Text(
                                currencyFormatter.format(resumo.custoTotalAtual),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column {
                            Text("Lucro Projetado (ROI)", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${currencyFormatter.format(resumo.lucroProjetado)} (${String.format(Locale.getDefault(), "%.1f", resumo.roiProjetadoPercentual)}%)",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (resumo.lucroProjetado >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Divisão de Aportes entre Sócios",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Registrado conforme as contas foram efetivadas por cada um:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    resumo.aportesPorSocio.forEach { aporte ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(
                                                runCatching {
                                                    Color(android.graphics.Color.parseColor(aporte.socio.corHex))
                                                }.getOrDefault(MaterialTheme.colorScheme.primary)
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = aporte.socio.nome,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${currencyFormatter.format(aporte.totalPago)} (${String.format(Locale.getDefault(), "%.1f", aporte.percentualDoTotal)}%)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (aporte.percentualDoTotal / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val totalEfetivado = resumo.totalEfetivado
                    Text(
                        text = "Total Pago/Efetivado Até o Momento: ${currencyFormatter.format(totalEfetivado)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

private fun Modifier.size(size: androidx.compose.ui.unit.Dp) = this.height(size).width(size)

@Composable
fun CustosTabContent(
    custos: List<Custo>,
    currencyFormatter: NumberFormat,
    onEfetivarClick: (Custo) -> Unit,
    onRemoverClick: (String) -> Unit
) {
    val custosPorCategoria = remember(custos) {
        custos.groupBy { it.categoria }.toSortedMap(compareBy { it.ordem })
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CategoriaCusto.entries.sortedBy { it.ordem }.forEach { categoria ->
            val listaCustosCat = custosPorCategoria[categoria] ?: emptyList()

            item {
                CategoriaGroupCard(
                    categoria = categoria,
                    custos = listaCustosCat,
                    currencyFormatter = currencyFormatter,
                    onEfetivarClick = onEfetivarClick,
                    onRemoverClick = onRemoverClick
                )
            }
        }
    }
}

@Composable
fun CategoriaGroupCard(
    categoria: CategoriaCusto,
    custos: List<Custo>,
    currencyFormatter: NumberFormat,
    onEfetivarClick: (Custo) -> Unit,
    onRemoverClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    val totalCatEfetivado = custos.filter { it.status == StatusCusto.EFETIVADO }.sumOf { it.valorEfetivado ?: 0.0 }
    val completados = custos.count { it.status == StatusCusto.EFETIVADO }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = categoria.titulo,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${completados}/${custos.size} pagas • Efetivado: ${currencyFormatter.format(totalCatEfetivado)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (custos.isEmpty()) {
                        Text(
                            text = "Nenhum custo cadastrado nesta etapa.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    } else {
                        custos.forEach { custo ->
                            CustoItemRow(
                                custo = custo,
                                currencyFormatter = currencyFormatter,
                                onEfetivarClick = { onEfetivarClick(custo) },
                                onRemoverClick = { onRemoverClick(custo.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustoItemRow(
    custo: Custo,
    currencyFormatter: NumberFormat,
    onEfetivarClick: () -> Unit,
    onRemoverClick: () -> Unit
) {
    val isEfetivado = custo.status == StatusCusto.EFETIVADO

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEfetivado) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isEfetivado) Icons.Default.CheckCircle else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (isEfetivado) Color(0xFF2E7D32) else Color(0xFFE65100),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = custo.titulo,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (isEfetivado) {
                    Text(
                        text = "Valor Pago: ${currencyFormatter.format(custo.valorEfetivado ?: 0.0)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "Efetivado por: ${custo.efetuadoPorSocioNome ?: "Desconhecido"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (custo.observacoes.isNotBlank()) {
                        Text(
                            text = "Obs: ${custo.observacoes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    Text(
                        text = "Valor Estimado: ${currencyFormatter.format(custo.valorEstimado)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isEfetivado) {
                    Button(
                        onClick = onEfetivarClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("Efetivar", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    OutlinedButton(
                        onClick = onEfetivarClick
                    ) {
                        Text("Editar", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(modifier = Modifier.width(2.dp))
                IconButton(onClick = onRemoverClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir Custo",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
