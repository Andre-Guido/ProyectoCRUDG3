package ni.edu.uam.proyectocrudg3.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ni.edu.uam.proyectocrudg3.connection.DatabaseConnection;
import ni.edu.uam.proyectocrudg3.models.Categoria;
import ni.edu.uam.proyectocrudg3.models.Marca;
import ni.edu.uam.proyectocrudg3.models.Producto;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class ProyectoCRUDController {
    @FXML
    private TextField txtCodigo;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtPrecio;
    @FXML
    private TextField txtStock;
    @FXML
    private ComboBox<Categoria> cmbCategoria;
    @FXML
    private ComboBox<Marca> cmbMarca;
    @FXML
    private DatePicker dtpFecha;
    @FXML
    private TextField txtDescripcion;
    @FXML
    private Button btnRegistrar;
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnActualizar;
    @FXML
    private Button btnEliminar;
    @FXML
    private TextField txtNombreFiltro;
    @FXML
    private ComboBox<Categoria> cmbCategoriaFiltro;
    @FXML
    private DatePicker dtpFechaIni;
    @FXML
    private DatePicker dtpFechaFin;
    @FXML
    private TextField txtPrecioMin;
    @FXML
    private TextField txtPrecioMax;
    @FXML
    private CheckBox chkProdExistencia;
    @FXML
    private ComboBox<String> cmbOrdenar;
    @FXML
    private TableView<Producto> tblProductos;
    @FXML
    private TableColumn<Producto, Integer> colId;
    @FXML
    private TableColumn<Producto, String> colCodigo;
    @FXML
    private TableColumn<Producto, String> colNombre;
    @FXML
    private TableColumn<Producto, String> colCategoria;
    @FXML
    private TableColumn<Producto, String> colMarca;
    @FXML
    private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML
    private TableColumn<Producto, Integer> colStock;
    @FXML
    private TableColumn<Producto, LocalDate> colFecha;
    @FXML
    private Button btnBuscar;
    @FXML
    private Button btnLimpiar;

    // Listas utilizadas por el TableView y los ComboBox
    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private final ObservableList<Categoria> listaCategorias = FXCollections.observableArrayList();
    private final ObservableList<Marca> listaMarcas = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        configurarTableView();
        configurarSeleccionTableView();
        cargarCategorias();
        cargarMarcas();
        cargarOpcionesOrden();
    }

    @FXML
    private void registrarProducto(){

    }

    @FXML
    private void guardarProducto(){

    }

    @FXML
    private void actualizarProducto(){

    }

    @FXML
    private void eliminarProducto(){

    }

    @FXML
    private void buscarProducto(){

    }

    @FXML
    private void limpiarFiltros(){
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtStock.clear();
        txtDescripcion.clear();
        txtPrecioMin.clear();
        txtPrecioMax.clear();
        dtpFecha.setValue(null);
        dtpFechaIni.setValue(null);
        dtpFechaFin.setValue(null);
        cmbCategoria.setValue(null);
        cmbMarca.setValue(null);
        cmbOrdenar.setValue(null);
        txtNombreFiltro.clear();
        cmbCategoriaFiltro.setValue(null);
        chkProdExistencia.setSelected(false);
    }

    // Estos son los métodos iniciales para empezar a trabajar

    private void configurarTableView(){
        colId.setCellValueFactory(data ->
                new SimpleIntegerProperty(
                        data.getValue().getIdProducto()
                ).asObject()
        );
        colCodigo.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCodigo()
                ));
        colNombre.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getNombre()
                )
        );
        colCategoria.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getCategoria().getNombre()
                )
        );
        colMarca.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getMarca().getNombre()
                )
        );
        colPrecio.setCellValueFactory(data ->
                new SimpleObjectProperty<>(
                        data.getValue().getPrecio()
                )
        );
        colStock.setCellValueFactory( data ->
                new SimpleIntegerProperty(
                        data.getValue().getStock()
                ).asObject()
        );
        colFecha.setCellValueFactory(data ->
                new SimpleObjectProperty<>(
                        data.getValue().getFechaCreacion()
                )
        );

        tblProductos.setItems(listaProductos);
    }


// Selección

    private void configurarSeleccionTableView(){
        tblProductos.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if(newValue != null){
                        cargarProductoEnFormulario(newValue);
                    }
                });
    }

    /**
     * Recibe como parámetro un producto seleccionado del TableView
     * @param p, es el producto seleccionado del TableView
     */
    private void cargarProductoEnFormulario(Producto p){
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtDescripcion.setText(p.getDescripcion());
        txtPrecio.setText(String.valueOf(p.getPrecio()));
        txtStock.setText(String.valueOf(p.getStock()));
        dtpFecha.setValue(p.getFechaCreacion());

        /**
         * Buscamos dentro del ComboBox el valor que corresponda al ID
         */

        for(Categoria categoria: listaCategorias){
            if(categoria.getIdCategoria() == p.getCategoria().getIdCategoria()){
                cmbCategoria.setValue(categoria);
                break;
            }
        }

        /**
         * Buscamos dentro del ComboBox el valor que corresponda al ID
         */
        for(Marca marca: listaMarcas){
            if(marca.getIdMarca() == p.getMarca().getIdMarca()){
                cmbMarca.setValue(marca);
                break;
            }
        }
    }

    /**
     * Consulta las categorias activas de la base de datos para mostrarlas dentro del ComboBox
     */
    private void cargarCategorias() {

        listaCategorias.clear();

        String sql = """
            SELECT
                id_categoria,
                nombre
            FROM catalogos.categorias
            WHERE estado = 1
            ORDER BY nombre
            """;

        try (
                Connection conexion =
                        DatabaseConnection.getConnection();

                PreparedStatement ps =
                        conexion.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                Categoria categoria =
                        new Categoria(
                                rs.getInt("id_categoria"),
                                rs.getString("nombre")
                        );

                listaCategorias.add(categoria);
            }


            /*
             * Utilizamos copias de la lista para evitar que
             * cada ComboBox tenga problemas de selección.
             */
            cmbCategoria.setItems(
                    FXCollections.observableArrayList(
                            listaCategorias
                    )
            );

            cmbCategoriaFiltro.setItems(
                    FXCollections.observableArrayList(
                            listaCategorias
                    )
            );

        } catch (SQLException e) {
            mostrarErrorBaseDatos(
                    "No se pudieron cargar las categorías.",
                    e
            );
        }
    }


    /**
     * Consulta las marcas activas almacenadas en PostgreSQL.
     *
     * Estas marcas se agregan al ComboBox utilizado en
     * el formulario de productos.
     */
    private void cargarMarcas() {

        listaMarcas.clear();

        String sql = """
            SELECT
                id_marca,
                nombre
            FROM catalogos.marcas
            WHERE estado = 1
            ORDER BY nombre
            """;

        try (
                Connection conexion =
                        DatabaseConnection.getConnection();

                PreparedStatement ps =
                        conexion.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {

                Marca marca =
                        new Marca(
                                rs.getInt("id_marca"),
                                rs.getString("nombre")
                        );

                listaMarcas.add(marca);
            }

            cmbMarca.setItems(
                    FXCollections.observableArrayList(
                            listaMarcas
                    )
            );
        } catch (SQLException e) {
            mostrarErrorBaseDatos(
                    "No se pudieron cargar las marcas.",
                    e
            );
        }
    }

    /**
     * Agrega al ComboBox las opciones disponibles para
     * ordenar los resultados.
     *
     * El usuario no introduce directamente el nombre
     * de una columna SQL.
     *
     * Esto permite utilizar una whitelist de valores
     * conocidos y evita concatenar texto arbitrario
     * dentro de ORDER BY.
     */
    private void cargarOpcionesOrden() {

        cmbOrdenar.setItems(
                FXCollections.observableArrayList(
                        "Nombre",
                        "Precio menor a mayor",
                        "Precio mayor a menor",
                        "Fecha más reciente",
                        "Fecha más antigua",
                        "Stock mayor a menor",
                        "Stock menor a mayor"
                )
        );

        cmbOrdenar.setValue("Nombre");
    }

    /**
     * Muestra una alerta cuando ocurre un error al consultar la base de datos.
     * @param mensaje, descripción del error para el usuario
     * @param e, excepción generada por la consulta
     */
    private void mostrarErrorBaseDatos(String mensaje, SQLException e) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error de base de datos");
        alerta.setHeaderText(mensaje);
        alerta.setContentText(e.getMessage());
        alerta.showAndWait();
    }

    // Crear producto desde resultset permite escribir los objetos coomo filas
    private Producto crearProductoDesdeResultSet(
            ResultSet rs
    ) throws SQLException {

        return new Producto(
                rs.getInt(
                        "id_producto"
                ),

                rs.getString(
                        "codigo"
                ),

                rs.getString(
                        "nombre"
                ),

                rs.getString(
                        "descripcion"
                ),

                rs.getBigDecimal(
                        "precio"
                ),

                rs.getInt(
                        "stock"
                ),

                rs.getDate(
                        "fecha_creacion"
                ).toLocalDate(),

                new Categoria(
                        rs.getInt("id_categoria"),
                        rs.getString("categoria")
                ),

                new Marca(
                        rs.getInt("id_marca"),
                        rs.getString("marca")
                ),

                rs.getInt(
                        "estado"
                )
        );
    }
}
