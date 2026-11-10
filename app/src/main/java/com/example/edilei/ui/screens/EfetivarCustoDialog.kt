package com.example.edilei.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.edilei.data.model.Custo
import com.example.edilei.data.model.Socio
import com.example.edilei.utils.formatBrl
import com.example.edilei.utils.formatBrlSemSimbolo
import com.example.edilei.utils.parseBrlToDouble

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EfetivarCustoDialog(
    custo: Custo,
    socios: List<Socio>,
    socioAtualId: String,
    onDismiss: () -> Unit,
    onConfirm: (valorEfetivado: Double, socioId: String, observacoes: String) -> Unit
) {
    var valorText by remember { mutableStateOf(custo.valorEstimado.formatBrlSemSimbolo()) }
    var selectedSocioId by remember { mutableStateOf(socioAtualId) }
    var observacoesText by remember { mutableStateOf("") }
    var expandedSocioDropdown by remember { mutableStateOf(false) }

    val socioSelecionado = socios.find { it.id == selectedSocioId } ?: socios.firstOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Efetivar Pagamento de Conta",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = custo.titulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Valor Estimado Inicial: ${custo.valorEstimado.formatBrl()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = valorText,
                    onValueChange = { valorText = it },
                    label = { Text("Valor Efetivo Pago (R$) (ex: 7.500,00)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedSocioDropdown,
                    onExpandedChange = { expandedSocioDropdown = !expandedSocioDropdown }
                ) {
                    OutlinedTextField(
                        value = socioSelecionado?.nome ?: "Selecione o sócio",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sócio Que Pagou / Efetuou PIX") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSocioDropdown) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedSocioDropdown,
                        onDismissRequest = { expandedSocioDropdown = false }
                    ) {
                        socios.forEach { socio ->
                            DropdownMenuItem(
                                text = { Text(socio.nome) },
                                onClick = {
                                    selectedSocioId = socio.id
                                    expandedSocioDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = observacoesText,
                    onValueChange = { observacoesText = it },
                    label = { Text("Observações / Nº Comprovante") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val valorDouble = valorText.parseBrlToDouble()
                    onConfirm(valorDouble, selectedSocioId, observacoesText)
                }
            ) {
                Text("Confirmar Efetivação")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
