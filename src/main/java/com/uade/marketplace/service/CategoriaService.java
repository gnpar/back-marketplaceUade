package com.uade.marketplace.service;

import com.uade.marketplace.dto.CategoriaRequestDTO;
import com.uade.marketplace.dto.CategoriaResponseDTO;
import com.uade.marketplace.model.Categoria;
import com.uade.marketplace.repository.CategoriaRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaResponseDTO> getAllCategorias() {
        List<Categoria> categorias = categoriaRepository.findAll();
        List<CategoriaResponseDTO> dtos = new ArrayList<>();

        for (Categoria categoria : categorias) {
            dtos.add(convertirADTO(categoria));
        }

        return dtos;
    }

    public CategoriaResponseDTO getCategoriaById(Long id) {
        Categoria categoria = categoriaRepository.findById(id).orElse(null);

        if (categoria == null) {
            return null;
        }

        return convertirADTO(categoria);
    }

    public CategoriaResponseDTO crearCategoria(CategoriaRequestDTO categoriaDTO) {
        if (categoriaRepository.existsByNombre(categoriaDTO.getNombre())) {
            throw new IllegalArgumentException(
                    "Ya existe una categoria con el nombre: " + categoriaDTO.getNombre());
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(categoriaDTO.getNombre());

        Categoria guardada = categoriaRepository.save(categoria);

        return convertirADTO(guardada);
    }

    public CategoriaResponseDTO actualizarCategoria(
            Long id, CategoriaRequestDTO categoriaDTO) {

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