package main.services;

import main.database.DAOs.CategoriaDAO;
import main.models.Categoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {
    @Autowired
    private CategoriaDAO categoriaDAO;

    public Categoria salvar(Categoria categoria) {
        return categoriaDAO.save(categoria);
    }

    public Optional<Categoria> obterPorId(Integer id) {
        return categoriaDAO.findById(id);
    }

    public List<Categoria> listarTodas() {
        return categoriaDAO.findAll();
    }

    public List<Categoria> findRaizes() {
        return categoriaDAO.findRaizes();
    }

    public List<Categoria> findByParent(Integer parentId) {
        return categoriaDAO.findByParent(parentId);
    }

    public void atualizar(Categoria categoria) {
        categoriaDAO.update(categoria);
    }

    public void deletar(Integer id) {
        categoriaDAO.deleteById(id);
    }

    public boolean existe(Integer id) {
        return categoriaDAO.findById(id).isPresent();
    }
}
