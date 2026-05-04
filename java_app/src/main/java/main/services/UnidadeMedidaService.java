package main.services;

import main.database.DAOs.UnidadeMedidaDAO;
import main.models.UnidadeMedida;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UnidadeMedidaService {
    @Autowired
    private UnidadeMedidaDAO unidadeMedidaDAO;

    public UnidadeMedida salvar(UnidadeMedida unidade) {
        return unidadeMedidaDAO.save(unidade);
    }

    public Optional<UnidadeMedida> obterPorId(Integer id) {
        return unidadeMedidaDAO.findById(id);
    }

    public List<UnidadeMedida> listarTodas() {
        return unidadeMedidaDAO.findAll();
    }

    public void atualizar(UnidadeMedida unidade) {
        unidadeMedidaDAO.update(unidade);
    }

    public void deletar(Integer id) {
        unidadeMedidaDAO.deleteById(id);
    }

    public boolean existe(Integer id) {
        return unidadeMedidaDAO.findById(id).isPresent();
    }
}