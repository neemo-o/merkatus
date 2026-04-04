package main.database.DAOs;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Component;

import main.database.GenericDAO;
import main.models.Produto;

@Component
public class ProdutoDAO extends GenericDAO<Produto, Integer> {

    public static java.util.List<Produto> findAllStatic() {
        return GenericDAO.findAllStatic(ProdutoDAO.class);
    }

    public static Produto findByIdStatic(Integer id) {
        return GenericDAO.findByIdStatic(ProdutoDAO.class, id);
    }

    public static boolean deleteByIdStatic(Integer id) {
        return GenericDAO.deleteByIdStatic(ProdutoDAO.class, id);
    }

    public static Produto insertStatic(Produto produto) {
        return GenericDAO.insertStatic(ProdutoDAO.class, produto);
    }

    public static boolean updateStatic(Produto produto) {
        return GenericDAO.updateStatic(ProdutoDAO.class, produto);
    }

    @Override
    protected String getTabela() {
        return "produto";
    }

    @Override
    protected String getColunaId() {
        return "id_produto";
    }

    @Override
    protected void setGeneratedId(Produto p, Number id) {
        p.setIdProduto(id.intValue());
    }

    @Override
    protected String getSqlInsert() {
        return """
                INSERT INTO produto
                (descricao, codigo_barras, unidade_medida, id_unidade_medida,
                 id_categoria, preco_custo, preco_venda, margem_lucro,
                 estoque_atual, estoque_minimo, estoque_maximo, id_fornecedor,
                 permite_fracionamento, controla_estoque, balanca, ativo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
    }

    @Override
    protected void setParametrosInsert(PreparedStatement stmt, Produto p) throws SQLException {
        stmt.setString(1, p.getDescricao()); // descricao — NOT NULL
        stmt.setString(2, p.getCodigoBarras()); // codigo_barras — pode ser null, mas setString aceita null
        stmt.setString(3, p.getUnidadeMedida()); // unidade_medida
        stmt.setObject(4, p.getIdUnidadeMedida()); // id_unidade_medida — Integer nullable, usa setObject
        stmt.setObject(5, p.getIdCategoria()); // id_categoria — Integer nullable
        stmt.setObject(6, p.getPrecoCusto());
        stmt.setObject(7, p.getPrecoVenda());
        stmt.setObject(8, p.getMargemLucro());
        stmt.setInt(9, p.getEstoqueAtual()); // INTEGER NOT NULL DEFAULT 0 — setInt
        stmt.setObject(10, p.getEstoqueMinimo()); // DECIMAL nullable — setObject ok
        stmt.setObject(11, p.getEstoqueMaximo()); // DECIMAL nullable — setObject ok
        stmt.setObject(12, p.getIdFornecedor()); // INTEGER nullable — setObject ok
        stmt.setBoolean(13, p.isPermiteFracionamento()); // BOOLEAN NOT NULL — setBoolean
        stmt.setBoolean(14, p.isControlaEstoque()); // BOOLEAN NOT NULL — setBoolean
        stmt.setBoolean(15, p.isBalanca()); // BOOLEAN NOT NULL — setBoolean
        stmt.setBoolean(16, p.isAtivo());
    }

    @Override
    protected void setParametrosUpdate(PreparedStatement stmt, Produto p) throws SQLException {
        stmt.setString(1, p.getDescricao()); // descricao — NOT NULL
        stmt.setString(2, p.getCodigoBarras()); // codigo_barras — pode ser null, mas setString aceita null
        stmt.setString(3, p.getUnidadeMedida()); // unidade_medida
        stmt.setObject(4, p.getIdUnidadeMedida()); // id_unidade_medida — Integer nullable, usa setObject
        stmt.setObject(5, p.getIdCategoria()); // id_categoria — Integer nullable
        stmt.setObject(6, p.getPrecoCusto());
        stmt.setObject(7, p.getPrecoVenda());
        stmt.setObject(8, p.getMargemLucro());
        stmt.setInt(9, p.getEstoqueAtual()); // INTEGER NOT NULL DEFAULT 0 — setInt
        stmt.setObject(10, p.getEstoqueMinimo()); // DECIMAL nullable — setObject ok
        stmt.setObject(11, p.getEstoqueMaximo()); // DECIMAL nullable — setObject ok
        stmt.setObject(12, p.getIdFornecedor()); // INTEGER nullable — setObject ok
        stmt.setBoolean(13, p.isPermiteFracionamento()); // BOOLEAN NOT NULL — setBoolean
        stmt.setBoolean(14, p.isControlaEstoque()); // BOOLEAN NOT NULL — setBoolean
        stmt.setBoolean(15, p.isBalanca()); // BOOLEAN NOT NULL — setBoolean
        stmt.setBoolean(16, p.isAtivo());
        stmt.setInt(17, p.getIdProduto()); // WHERE
    }

    @Override
    protected String getSqlUpdate() {
        return """
                UPDATE produto SET
                    descricao = ?,
                    codigo_barras = ?,
                    unidade_medida = ?,
                    id_unidade_medida = ?,
                    id_categoria = ?,
                    preco_custo = ?,
                    preco_venda = ?,
                    margem_lucro = ?,
                    estoque_atual = ?,
                    estoque_minimo = ?,
                    estoque_maximo = ?,
                    id_fornecedor = ?,
                    permite_fracionamento = ?,
                    controla_estoque = ?,
                    balanca = ?,
                    ativo = ?
                WHERE id_produto = ?
                    """;
    }

    @Override
    protected Produto mapear(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setIdProduto(rs.getInt("id_produto"));
        p.setDescricao(rs.getString("descricao"));
        p.setCodigoBarras(rs.getString("codigo_barras"));
        p.setUnidadeMedida(rs.getString("unidade_medida"));
        p.setIdUnidadeMedida(rs.getObject("id_unidade_medida", Integer.class));
        p.setIdCategoria(rs.getObject("id_categoria", Integer.class));
        p.setPrecoCusto(rs.getBigDecimal("preco_custo"));
        p.setPrecoVenda(rs.getBigDecimal("preco_venda"));
        p.setMargemLucro(rs.getBigDecimal("margem_lucro"));
        p.setEstoqueAtual(rs.getInt("estoque_atual"));
        p.setEstoqueMinimo(rs.getBigDecimal("estoque_minimo"));
        p.setEstoqueMaximo(rs.getBigDecimal("estoque_maximo"));
        p.setIdFornecedor(rs.getObject("id_fornecedor", Integer.class));
        p.setPermiteFracionamento(rs.getBoolean("permite_fracionamento"));
        p.setControlaEstoque(rs.getBoolean("controla_estoque"));
        p.setBalanca(rs.getBoolean("balanca"));
        p.setAtivo(rs.getBoolean("ativo"));
        return p;
    }
}
