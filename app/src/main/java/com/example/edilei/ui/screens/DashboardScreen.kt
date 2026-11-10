package com.example.edilei.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.edilei.data.model.Imovel
import com.example.edilei.data.model.Socio
import com.example.edilei.data.model.StatusImovel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    imoveis: List<Imovel>,
    socios: List<Socio>,
    socioAtualId: String,
    onSelectSocio: (String) -> Unit,
    onSelectImovel: (Imovel) -> Unit,
    onEditarImovelClick: (Imovel) -> Unit,
    onAdicionarImovelClick: () -> Unit,
    onSalvarSocio: (id: String?, nome: String, email: String, corHex: String) -> Unit
) {
    var showGerenciarSociosDialog by remember { mutableStateOf(false) }
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")) }

    val totalInvestidoEstimado = imoveis.sumOf { it.valorLanceArrematado }
    val totalVendaPretendida = imoveis.sumOf { it.valorVendaPretendido }
    val lucroTotalProjetado = totalVendaPretendida - totalInvestidoEstimado

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HomeWork,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EdiLei - Leilões",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        IconButton(onClick = { showGerenciarSociosDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = "Gerenciar Sócios",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdicionarImovelClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Novo Imóvel")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Seletor de Sócio Ativo no Aparelho
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sócio Ativo neste Dispositivo:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            OutlinedButton(
                                onClick = { showGerenciarSociosDialog = true },
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("+ Editar Sócios", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            socios.forEach { socio ->
                                FilterChip(
                                    selected = socio.id == socioAtualId,
                                    onClick = { onSelectSocio(socio.id) },
                                    label = { Text(socio.nome) }
                                )
                            }
                        }
                    }
                }
            }

            // Resumo Geral da Carteira
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Carteira de Imóveis (${imoveis.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Lances Arrematados", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    currencyFormatter.format(totalInvestidoEstimado),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text("Projeção de Lucro", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    currencyFormatter.format(lucroTotalProjetado),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Imóveis Arrematados e em Gestão",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (imoveis.isEmpty()) {
                item {
                    Text(
                        text = "Nenhum imóvel cadastrado. Clique no botão '+' para cadastrar o primeiro arremate.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(imoveis, key = { it.id }) { imovel ->
                    ImovelCardRow(
                        imovel = imovel,
                        currencyFormatter = currencyFormatter,
                        onClick = { onSelectImovel(imovel) },
                        onEditarClick = { onEditarImovelClick(imovel) }
                    )
                }
            }
        }

        if (showGerenciarSociosDialog) {
            GerenciarSociosDialog(
                socios = socios,
                onDismiss = { showGerenciarSociosDialog = false },
                onSalvarSocio = { id, nome, email, cor ->
                    onSalvarSocio(id, nome, email, cor)
                }
            )
        }
    }
}

@Composable
fun ImovelCardRow(
    imovel: Imovel,
    currencyFormatter: NumberFormat,
    onClick: () -> Unit,
    onEditarClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = imovel.status)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = imovel.bancoOrigem,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onEditarClick,
                        modifier = Modifier.height(24.dp).width(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar Imóvel",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = imovel.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = imovel.enderecoCompleto,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Valor Lance", style = MaterialTheme.typography.bodySmall)
                    Text(
                        currencyFormatter.format(imovel.valorLanceArrematado),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text("Venda Pretendida", style = MaterialTheme.typography.bodySmall)
                    Text(
                        currencyFormatter.format(imovel.valorVendaPretendido),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: StatusImovel) {
    val (bgColor, textColor) = when (status) {
        StatusImovel.EM_ANALISE -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        StatusImovel.ARREMATADO -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        StatusImovel.DESOCUPACAO -> Color(0xFFF3E5F5) to Color(0xFF7B1FA2)
        StatusImovel.REFORMA -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        StatusImovel.A_VENDA -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
        StatusImovel.VENDIDO -> Color(0xFFE0F2F1) to Color(0xFF00695C)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
