package com.uade.marketplace.service;

import com.uade.marketplace.dto.CategoriaRequestDTO;
import com.uade.marketplace.dto.CategoriaResponseDTO;
import com.uade.marketplace.model.Categoria;
import com.uade.marketplace.repository.CategoriaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public CategoriaResponseDTO actualizarCategoria(Long id, CategoriaRequestDTO categoriaDTO) {
        Categoria categoria = categoriaRepository.findById(id).orElse(null);
        if (categoria == null) {
            return null;
        }
        categoria.setNombre(categoriaDTO.getNombre());

        Categoria guardada = categoriaRepository.save(categoria);

        return convertirADTO(guardada);
    }

    public boolean eliminarCategoria(Long id) {
        if (!categoriaRepository.existsById(id)) {
            return false;
        }
        categoriaRepository.deleteById(id);
        return true;
    }

    private CategoriaResponseDTO convertirADTO(Categoria categoria) {
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        return dto;
    }

}