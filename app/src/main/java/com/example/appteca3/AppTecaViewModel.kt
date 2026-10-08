package com.example.appteca3

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.collections.emptyList

class AppTecaViewModel : ViewModel() {
    // ── El estado interno (antes: variables de la Activity) ──
    private var query = ""
    private var soloFavoritas = false

    private val _appSeleccionada = MutableStateFlow<App?>(null)
    val appSeleccionada: StateFlow<App?> = _appSeleccionada

    fun seleccionar(app: App) { _appSeleccionada.value = app }
    fun volverALista() { _appSeleccionada.value = null }

    // ── El estado publicado (antes: no existía — cada listener redibujaba) ──
    private val _listaVisible = MutableStateFlow<List<App>>(emptyList())
    val listaVisible: StateFlow<List<App>> = _listaVisible
    private val _modoSoloFavoritas = MutableStateFlow(false)
    val modoSoloFavoritas: StateFlow<Boolean> = _modoSoloFavoritas

    init {
        Log.d("VIDA", "ViewModel → creado (${hashCode()})")
        aplicarFiltros()
    }

    // ── Los eventos que la pantalla puede avisar ──
    fun buscar(texto: String) {
        query = texto.trim()
        aplicarFiltros()
    }

    fun alternarModo() {
        soloFavoritas = !soloFavoritas
        aplicarFiltros()
    }

    fun alternarFavorita(app: App) {
        val nuevas = Catalogo.apps.map {
            if (it.id == app.id) it.copy(esFavorita = !it.esFavorita) else it
        }
        Catalogo.apps.clear()
        Catalogo.apps.addAll(nuevas)
        aplicarFiltros()
    }

    // ── LA función, mudada casi textual — con una diferencia decisiva ──
    private fun aplicarFiltros() {
        var lista: List<App> = Catalogo.apps
        if (query.isNotEmpty()) lista = lista.filter {
            it.nombre.contains(query, true) || it.categoria.contains(query, true)
        }
        if (soloFavoritas) lista = lista.filter { it.esFavorita }
        _listaVisible.value = lista // publica — no dibuja
        _modoSoloFavoritas.value = soloFavoritas

        _appSeleccionada.value = _appSeleccionada.value?.let { sel ->
            Catalogo.apps.find { it.id == sel.id }
        }
    }

    override fun onCleared() {
        Log.d("VIDA", "ViewModel → onCleared")
    }
}
