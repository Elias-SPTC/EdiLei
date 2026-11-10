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
import com.example.edilei.data.model.CategoriaCusto
import com.example.edilei.utils.parseBrlToDouble

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustoDialog(
    onDismiss: () -> Unit,
    onConfirm: (titulo: String, categoria: CategoriaCusto, valorEstimado: Double) -> Unit
) {
    var tituloText by remember { mutableStateOf("") }
    var selectedCategoria by remember { mutableStateOf(CategoriaCusto.MANUTENCAO_REFORMA) }
    var valorText by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Adicionar Novo Custo / Conta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = tituloText,
                    onValueChange = { tituloText = it },
                    label = { Text("Título da Conta / Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedCategoria.titulo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Etapa do Leilão / Categoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        CategoriaCusto.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.titulo) },
                                onClick = {
                                    selectedCategoria = cat
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = valorText,
                    onValueChange = { valorText = it },
                    label = { Text("Valor Estimado Inicial (R$) (ex: 3.500,00)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                enabled = tituloText.isNotBlank() && valorText.isNotBlank(),
                onClick = {
                    val valor = valorText.parseBrlToDouble()
                    onConfirm(tituloText, selectedCategoria, valor)
                }
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
