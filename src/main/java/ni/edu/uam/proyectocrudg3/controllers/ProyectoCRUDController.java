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
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("ALL")
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
        cargarProductos();
    }

    @FXML
    private void registrarProducto(){

    }

    @FXML
    private void guardarProducto(){
        // Si los datos del formulario no son válidos, no se continúa
        if (!validarFormulario()) {
            // Sale del método sin guardar
            return;
        }
        // Obtiene el código escrito por el usuario, sin espacios al inicio ni al final
        String codigo =
                txtCodigo.getText().trim();
        /*
         * Validación preventiva de UNIQUE.
         * Verifica que ningún otro producto tenga ya este código;
         * si existe, se avisa al usuario y no se guarda.
         */
        if (existeCodigo(codigo, null)) {
            // Avisa al usuario que el código ya pertenece a otro producto
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Código duplicado",
                    "El código " + codigo +
                            " ya pertenece a otro producto."
            );
            // Coloca el cursor en el campo código para corregirlo
            txtCodigo.requestFocus();
            // Sale del método sin guardar
            return;
        }
        /*
         * Sentencia INSERT que registra el nuevo producto:
         * - INSERT INTO catalogos.productos (...): tabla y columnas que se llenan.
         * - VALUES (?, ?, ...): cada "?" se reemplaza por un dato del formulario
         *   y el estado se guarda en 1 (activo).
         */
        String sql = """
                INSERT INTO catalogos.productos
                (
                    codigo,
                    nombre,
                    descripcion,
                    precio,
                    stock,
                    fecha_creacion,
                    id_categoria,
                    id_marca,
                    estado
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)
                """;
        // Abre la conexión y prepara la sentencia; ambas se cierran solas al terminar el try
        try (
                // Obtiene una conexión con PostgreSQL
                Connection conexion =
                        DatabaseConnection.getConnection();
                // Prepara la sentencia INSERT con sus parámetros
                PreparedStatement ps =
                        conexion.prepareStatement(sql)
        ) {
            // Parámetro 1: código del producto
            ps.setString(
                    1,
                    codigo
            );
            // Parámetro 2: nombre del producto
            ps.setString(
                    2,
                    txtNombre.getText().trim()
            );
            // Parámetro 3: descripción del producto
            ps.setString(
                    3,
                    txtDescripcion.getText().trim()
            );
            // Parámetro 4: precio, convertido de texto a BigDecimal
            ps.setBigDecimal(
                    4,
                    new BigDecimal(
                            txtPrecio.getText().trim()
                    )
            );
            // Parámetro 5: stock, convertido de texto a entero
            ps.setInt(
                    5,
                    Integer.parseInt(
                            txtStock.getText().trim()
                    )
            );
            // Parámetro 6: fecha, convertida de LocalDate a java.sql.Date
            ps.setDate(
                    6,
                    Date.valueOf(
                            dtpFecha.getValue()
                    )
            );
            // Parámetro 7: id de la categoría seleccionada en el ComboBox
            ps.setInt(
                    7,
                    cmbCategoria.getValue()
                            .getIdCategoria()
            );
            // Parámetro 8: id de la marca seleccionada en el ComboBox
            ps.setInt(
                    8,
                    cmbMarca.getValue()
                            .getIdMarca()
            );
            // Ejecuta el INSERT y obtiene cuántas filas se insertaron
            int filasAfectadas =
                    ps.executeUpdate();
            // Si se insertó al menos una fila, el registro fue exitoso
            if (filasAfectadas > 0) {
                // Informa al usuario que el producto se guardó
                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Registro guardado",
                        "El producto fue registrado correctamente."
                );
                // Limpia el formulario y los filtros de búsqueda
                limpiarFiltros();
                // Recarga la tabla para mostrar el nuevo producto
                cargarProductos();
            }
        } catch (SQLException e) {
            /*
             * Aunque JavaFX realizó previamente la comprobación,
             * PostgreSQL continúa siendo la autoridad final.
             *
             * SQLState 23505 corresponde normalmente a una
             * violación de UNIQUE en PostgreSQL.
             */
            if ("23505".equals(e.getSQLState())) {
                // Avisa que PostgreSQL rechazó el código por estar repetido
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Registro duplicado",
                        "El código ingresado ya existe."
                );
            } else {
                // Cualquier otro error se muestra con el detalle de la excepción
                mostrarErrorBaseDatos(
                        "No se pudo guardar el producto.",
                        e
                );
            }
        }
    }

    @FXML
    private void actualizarProducto(){
        // Obtiene el producto seleccionado en el TableView
        Producto productoSeleccionado =
                tblProductos.getSelectionModel()
                        .getSelectedItem();
        // Si no hay un producto seleccionado, no se puede actualizar
        if (productoSeleccionado == null) {
            // Pide al usuario que seleccione un producto
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Producto no seleccionado",
                    "Seleccione un producto del TableView antes de actualizar."
            );
            // Sale del método sin actualizar
            return;
        }
        // Si los datos del formulario no son válidos, no se continúa
        if (!validarFormulario()) {
            // Sale del método sin actualizar
            return;
        }
        // Obtiene el código escrito por el usuario, sin espacios al inicio ni al final
        String codigo =
                txtCodigo.getText().trim();
        /*
         * Se comprueba que otro producto diferente
         * no esté utilizando el mismo código.
         */
        if (existeCodigo(
                codigo,
                productoSeleccionado.getIdProducto()
        )) {
            // Avisa al usuario que el código ya pertenece a otro producto
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Código duplicado",
                    "El código ingresado pertenece a otro producto."
            );
            // Coloca el cursor en el campo código para corregirlo
            txtCodigo.requestFocus();
            // Sale del método sin actualizar
            return;
        }
        /*
         * Sentencia UPDATE que modifica el producto:
         * - UPDATE catalogos.productos: tabla que se modifica.
         * - SET ...: columnas que reciben los nuevos datos del formulario.
         * - WHERE id_producto = ?: solo se modifica el producto seleccionado.
         */
        String sql = """
                UPDATE catalogos.productos
                SET
                    codigo = ?,
                    nombre = ?,
                    descripcion = ?,
                    precio = ?,
                    stock = ?,
                    fecha_creacion = ?,
                    id_categoria = ?,
                    id_marca = ?

                WHERE id_producto = ?
                """;
        // Abre la conexión y prepara la sentencia; ambas se cierran solas al terminar el try
        try (
                // Obtiene una conexión con PostgreSQL
                Connection conexion =
                        DatabaseConnection.getConnection();

                // Prepara la sentencia UPDATE con sus parámetros
                PreparedStatement ps =
                        conexion.prepareStatement(sql)
        ) {
            // Parámetro 1: código del producto
            ps.setString(
                    1,
                    codigo
            );
            // Parámetro 2: nombre del producto
            ps.setString(
                    2,
                    txtNombre.getText().trim()
            );
            // Parámetro 3: descripción del producto
            ps.setString(
                    3,
                    txtDescripcion.getText().trim()
            );
            // Parámetro 4: precio, convertido de texto a BigDecimal
            ps.setBigDecimal(
                    4,
                    new BigDecimal(
                            txtPrecio.getText().trim()
                    )
            );
            // Parámetro 5: stock, convertido de texto a entero
            ps.setInt(
                    5,
                    Integer.parseInt(
                            txtStock.getText().trim()
                    )
            );
            // Parámetro 6: fecha, convertida de LocalDate a java.sql.Date
            ps.setDate(
                    6,
                    Date.valueOf(
                            dtpFecha.getValue()
                    )
            );
            // Parámetro 7: id de la categoría seleccionada en el ComboBox
            ps.setInt(
                    7,
                    cmbCategoria.getValue()
                            .getIdCategoria()
            );
            // Parámetro 8: id de la marca seleccionada en el ComboBox
            ps.setInt(
                    8,
                    cmbMarca.getValue()
                            .getIdMarca()
            );
            // Parámetro 9: id del producto que se actualiza (condición del WHERE)
            ps.setInt(
                    9,
                    productoSeleccionado.getIdProducto()
            );
            // Ejecuta el UPDATE y obtiene cuántas filas se modificaron
            int filasAfectadas =
                    ps.executeUpdate();
            // Si se modificó al menos una fila, la actualización fue exitosa
            if (filasAfectadas > 0) {
                // Informa al usuario que el producto se actualizó
                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Registro actualizado",
                        "El producto fue actualizado correctamente."
                );
                // Limpia el formulario y los filtros de búsqueda
                limpiarFiltros();
                // Recarga la tabla para mostrar los cambios
                cargarProductos();
            }
        } catch (SQLException e) {
            // SQLState 23505: PostgreSQL rechazó el código por estar repetido (UNIQUE)
            if ("23505".equals(e.getSQLState())) {
                // Avisa al usuario que el código ya está en uso
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Código duplicado",
                        "El código ingresado ya está siendo utilizado."
                );
            } else {
                // Cualquier otro error se muestra con el detalle de la excepción
                mostrarErrorBaseDatos(
                        "No se pudo actualizar el producto.",
                        e
                );
            }
        }
    }

    @FXML
    private void eliminarProducto(){
        // Obtiene el producto seleccionado en el TableView
        Producto productoSeleccionado =
                tblProductos.getSelectionModel()
                        .getSelectedItem();
        // Si no hay un producto seleccionado, no se puede eliminar
        if (productoSeleccionado == null) {
            // Pide al usuario que seleccione un producto
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Producto no seleccionado",
                    "Seleccione un producto antes de eliminarlo."
            );
            // Sale del método sin eliminar
            return;
        }
        /*
         * Se solicita confirmación para evitar que el usuario
         * desactive accidentalmente un registro.
         */
        Alert confirmacion =
                new Alert(Alert.AlertType.CONFIRMATION);
        // Título de la ventana de confirmación
        confirmacion.setTitle(
                "Confirmar eliminación"
        );
        // Encabezado de la ventana de confirmación
        confirmacion.setHeaderText(
                "Eliminar producto"
        );
        // Pregunta si desea desactivar el producto, mostrando su nombre
        confirmacion.setContentText(
                "¿Desea desactivar el producto "
                        + productoSeleccionado.getNombre()
                        + "?"
        );
        // Muestra la ventana y espera la respuesta del usuario
        Optional<ButtonType> respuesta =
                confirmacion.showAndWait();
        // Si el usuario cerró la ventana o no presionó Aceptar, se cancela
        if (
                respuesta.isEmpty()
                        ||
                        respuesta.get() != ButtonType.OK
        ) {
            // Sale del método sin eliminar
            return;
        }
        /*
         * Sentencia UPDATE que elimina el producto de forma lógica:
         * - UPDATE catalogos.productos: tabla que se modifica.
         * - SET estado = 0: marca el producto como inactivo en lugar de borrarlo.
         * - WHERE id_producto = ?: solo se desactiva el producto seleccionado.
         */
        String sql = """
                UPDATE catalogos.productos
                SET estado = 0
                WHERE id_producto = ?
                """;
        // Abre la conexión y prepara la sentencia; ambas se cierran solas al terminar el try
        try (
                // Obtiene una conexión con PostgreSQL
                Connection conexion =
                        DatabaseConnection.getConnection();
                // Prepara la sentencia UPDATE con su parámetro
                PreparedStatement ps =
                        conexion.prepareStatement(sql)
        ) {
            // Parámetro 1: id del producto que se va a desactivar
            ps.setInt(
                    1,
                    productoSeleccionado.getIdProducto()
            );
            // Ejecuta el UPDATE y obtiene cuántas filas se modificaron
            int filasAfectadas =
                    ps.executeUpdate();
            // Si se modificó al menos una fila, el producto quedó desactivado
            if (filasAfectadas > 0) {
                // Informa al usuario que el producto se desactivó
                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Producto eliminado",
                        "El producto fue desactivado correctamente."
                );
                // Limpia el formulario y los filtros de búsqueda
                limpiarFiltros();
                // Recarga la tabla; el producto desactivado ya no aparece
                cargarProductos();
            }
        } catch (SQLException e) {
            // Si ocurre un error de base de datos, se muestra con el detalle de la excepción
            mostrarErrorBaseDatos(
                    "No se pudo eliminar el producto.",
                    e
            );
        }
    }

    @FXML
    private void buscarProducto(){
        // Vacía la lista para mostrar solo los resultados de esta búsqueda
        listaProductos.clear();
        /*
         * La consulta inicia con los INNER JOIN necesarios
         * y con una condición permanente:
         *
         * p.estado = 1
         *
         * De esta forma nunca mostramos productos
         * eliminados lógicamente.
         *
         * Cláusulas de la consulta base:
         * - SELECT: columnas del producto, de su categoría y de su marca.
         * - FROM catalogos.productos p: tabla principal (alias p).
         * - INNER JOIN categorias c / marcas m: une cada producto con
         *   su categoría y su marca para obtener sus nombres.
         * - WHERE p.estado = 1: solo productos activos; los demás
         *   filtros se agregan después con AND.
         */
        StringBuilder sql = new StringBuilder("""
                SELECT
                    p.id_producto,
                    p.codigo,
                    p.nombre,
                    p.descripcion,
                    p.precio,
                    p.stock,
                    p.fecha_creacion,
                    c.id_categoria,
                    c.nombre AS categoria,
                    m.id_marca,
                    m.nombre AS marca,
                    p.estado
                FROM catalogos.productos p
                INNER JOIN catalogos.categorias c
                    ON p.id_categoria = c.id_categoria
                INNER JOIN catalogos.marcas m
                    ON p.id_marca = m.id_marca
                WHERE p.estado = 1
                """);



        /*
         * Aquí almacenaremos los valores que posteriormente
         * serán enviados al PreparedStatement.
         */
        List<Object> parametros =
                new ArrayList<>();
        // Obtiene el texto del filtro de código o nombre
        String texto =
                txtNombreFiltro.getText().trim();



        // Si el usuario escribió un texto, se filtra por código o nombre
        if (!texto.isBlank()) {
            /*
             * OR permite que el texto coincida con código
             * o nombre.
             *
             * ILIKE permite búsquedas parciales sin importar
             * mayúsculas y minúsculas.
             */
            sql.append("""
                   
                    AND (
                        p.codigo ILIKE ?
                        OR p.nombre ILIKE ?
                    )
                    """);



            // Valor para p.codigo ILIKE ?; los % permiten coincidencias parciales
            parametros.add(
                    "%" + texto + "%"
            );
            // Valor para p.nombre ILIKE ?
            parametros.add(
                    "%" + texto + "%"
            );
        }
        // Obtiene la categoría seleccionada en el filtro
        Categoria categoria =
                cmbCategoriaFiltro.getValue();
        // Si se eligió una categoría, se filtra por ella
        if (categoria != null) {
            // Agrega la condición de categoría a la consulta
            sql.append("""
                   
                    AND p.id_categoria = ?
                    """);
            // Valor para p.id_categoria = ?
            parametros.add(
                    categoria.getIdCategoria()
            );
        }
        // Obtiene la fecha inicial del filtro
        LocalDate fechaDesde =
                dtpFechaIni.getValue();
        // Si hay fecha inicial, se buscan productos creados desde esa fecha
        if (fechaDesde != null) {
            // Agrega la condición de fecha mínima
            sql.append("""
                   
                    AND p.fecha_creacion >= ?
                    """);
            // Valor para p.fecha_creacion >= ?
            parametros.add(fechaDesde);
        }
        // Obtiene la fecha final del filtro
        LocalDate fechaHasta =
                dtpFechaFin.getValue();
        // Si hay fecha final, se buscan productos creados hasta esa fecha
        if (fechaHasta != null) {
            // Agrega la condición de fecha máxima
            sql.append("""
                   
                    AND p.fecha_creacion <= ?
                    """);
            // Valor para p.fecha_creacion <= ?
            parametros.add(fechaHasta);
        }
        /*
         * Verificamos que las fechas tengan sentido.
         *
         * "Desde" no debería ser posterior a "Hasta".
         */
        if (
                fechaDesde != null
                        &&
                        fechaHasta != null
                        &&
                        fechaDesde.isAfter(fechaHasta)
        ) {
            // Avisa que la fecha inicial es posterior a la final
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Rango de fechas incorrecto",
                    "La fecha inicial no puede ser posterior a la fecha final."
            );
            // Cancela la búsqueda
            return;
        }
        // Precio mínimo del filtro; queda en null si el campo está vacío
        BigDecimal precioMinimo = null;
        // Si se escribió un precio mínimo, se valida y se agrega como filtro
        if (!txtPrecioMin.getText().isBlank()) {
            // Se usa try porque el texto escrito podría no ser un número
            try {
                // Convierte el texto del precio mínimo a BigDecimal
                precioMinimo =
                        new BigDecimal(
                                txtPrecioMin
                                        .getText()
                                        .trim()
                        );
                // Verifica que el precio mínimo no sea negativo
                if (
                        precioMinimo.compareTo(
                                BigDecimal.ZERO
                        ) < 0
                ) {
                    // Avisa que el precio mínimo es negativo
                    mostrarAlerta(
                            Alert.AlertType.WARNING,
                            "Precio incorrecto",
                            "El precio mínimo no puede ser negativo."
                    );
                    // Cancela la búsqueda
                    return;
                }
                // Agrega la condición de precio mínimo
                sql.append("""
                       
                        AND p.precio >= ?
                        """);
                // Valor para p.precio >= ?
                parametros.add(precioMinimo);
            } catch (NumberFormatException e) {
                // Si el texto no es numérico, se avisa al usuario
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Precio incorrecto",
                        "El precio mínimo debe ser numérico."
                );
                // Coloca el cursor en el campo de precio mínimo
                txtPrecioMin.requestFocus();
                // Cancela la búsqueda
                return;
            }
        }
        // Precio máximo del filtro; queda en null si el campo está vacío
        BigDecimal precioMaximo = null;
        // Si se escribió un precio máximo, se valida y se agrega como filtro
        if (!txtPrecioMax.getText().isBlank()) {
            // Se usa try porque el texto escrito podría no ser un número
            try {
                // Convierte el texto del precio máximo a BigDecimal
                precioMaximo =
                        new BigDecimal(
                                txtPrecioMax
                                        .getText()
                                        .trim()
                        );
                // Verifica que el precio máximo no sea negativo
                if (
                        precioMaximo.compareTo(
                                BigDecimal.ZERO
                        ) < 0
                ) {
                    // Avisa que el precio máximo es negativo
                    mostrarAlerta(
                            Alert.AlertType.WARNING,
                            "Precio incorrecto",
                            "El precio máximo no puede ser negativo."
                    );
                    // Cancela la búsqueda
                    return;
                }
                // Agrega la condición de precio máximo
                sql.append("""
                       
                        AND p.precio <= ?
                        """);
                // Valor para p.precio <= ?
                parametros.add(precioMaximo);
            } catch (NumberFormatException e) {
                // Si el texto no es numérico, se avisa al usuario
                mostrarAlerta(
                        Alert.AlertType.WARNING,
                        "Precio incorrecto",
                        "El precio máximo debe ser numérico."
                );
                // Coloca el cursor en el campo de precio máximo
                txtPrecioMax.requestFocus();
                // Cancela la búsqueda
                return;
            }
        }
        /*
         * Validamos que el precio mínimo no sea superior
         * al precio máximo.
         */
        if (
                precioMinimo != null
                        &&
                        precioMaximo != null
                        &&
                        precioMinimo.compareTo(
                                precioMaximo
                        ) > 0
        ) {
            // Avisa que el precio mínimo es mayor que el máximo
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Rango de precios incorrecto",
                    "El precio mínimo no puede ser mayor que el precio máximo."
            );
            // Cancela la búsqueda
            return;
        }
        // Si está marcado "Solo productos con existencias", se filtra por stock
        if (chkProdExistencia.isSelected()) {
            // Agrega la condición de stock mayor que cero
            sql.append("""
                   
                    AND p.stock > 0
                    """);
        }
        // Obtiene la opción de ordenamiento elegida
        String orden =
                cmbOrdenar.getValue();

        // Si no se eligió ninguna opción, se usa el orden por nombre
        if (orden == null) {
            // Asigna "Nombre" como orden predeterminado
            orden = "Nombre";
        }
        // Agrega el ORDER BY según la opción elegida (solo opciones fijas, sin texto del usuario)
        switch (orden) {
            // Ordena por precio de menor a mayor
            case "Precio menor a mayor" ->
                    sql.append("""
                           
                            ORDER BY p.precio ASC
                            """);
            // Ordena por precio de mayor a menor
            case "Precio mayor a menor" ->
                    sql.append("""
                           
                            ORDER BY p.precio DESC
                            """);
            // Ordena de la fecha más reciente a la más antigua
            case "Fecha más reciente" ->
                    sql.append("""
                           
                            ORDER BY p.fecha_creacion DESC
                            """);
            // Ordena de la fecha más antigua a la más reciente
            case "Fecha más antigua" ->
                    sql.append("""
                           
                            ORDER BY p.fecha_creacion ASC
                            """);
            // Ordena por stock de mayor a menor
            case "Stock mayor a menor" ->
                    sql.append("""
                           
                            ORDER BY p.stock DESC
                            """);
            // Ordena por stock de menor a mayor
            case "Stock menor a mayor" ->
                    sql.append("""
                           
                            ORDER BY p.stock ASC
                            """);
            // Cualquier otra opción (incluida "Nombre"): orden alfabético por nombre
            default ->
                    sql.append("""
                           
                            ORDER BY p.nombre ASC
                            """);
        }
        // Abre la conexión y prepara la consulta; ambas se cierran solas al terminar el try
        try (
                // Obtiene una conexión con PostgreSQL
                Connection conexion =
                        DatabaseConnection.getConnection();
                // Prepara la consulta armada con todos los filtros
                PreparedStatement ps =
                        conexion.prepareStatement(
                                sql.toString()
                        )
        ) {
            /*
             * Recorremos la lista de parámetros para
             * asignarlos dinámicamente al PreparedStatement.
             */
            for (
                    int i = 0;
                    i < parametros.size();
                    i++
            ) {
                // Obtiene el valor del parámetro en la posición i
                Object valor =
                        parametros.get(i);
                // Asigna el valor según su tipo; en JDBC los parámetros empiezan en 1, por eso i + 1
                if (valor instanceof LocalDate fecha) {
                    // Las fechas se convierten de LocalDate a java.sql.Date
                    ps.setDate(
                            i + 1,
                            Date.valueOf(fecha)
                    );
                } else if (valor instanceof BigDecimal numero) {
                    // Los precios se envían como BigDecimal
                    ps.setBigDecimal(
                            i + 1,
                            numero
                    );
                } else {
                    // El texto y los id se envían con setObject
                    ps.setObject(
                            i + 1,
                            valor
                    );
                }
            }
            // Ejecuta la consulta; el ResultSet se cierra solo al terminar el try
            try (
                    // Obtiene las filas que cumplen con los filtros
                    ResultSet rs =
                            ps.executeQuery()
            ) {
                // Recorre cada fila del resultado
                while (rs.next()) {
                    // Convierte la fila en un Producto y lo agrega a la lista
                    listaProductos.add(
                            crearProductoDesdeResultSet(rs)
                    );
                }
            }
            // Muestra la lista de resultados en el TableView
            tblProductos.setItems(
                    listaProductos
            );
            // Si no hubo resultados, se informa al usuario
            if (listaProductos.isEmpty()) {
                // Muestra el mensaje "Sin resultados"
                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Sin resultados",
                        "No existen productos que coincidan con los filtros aplicados."
                );
            }
        } catch (SQLException e) {
            // Si ocurre un error de base de datos, se muestra con el detalle de la excepción
            mostrarErrorBaseDatos(
                    "No se pudo realizar la búsqueda.",
                    e
            );
        }
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
    /**
     * Consulta los productos activos y los muestra en el TableView.
     *
     * Se utiliza después de guardar, actualizar o eliminar
     * para refrescar la información de la tabla.
     */
    private void cargarProductos() {
        // Vacía la lista antes de volver a llenarla
        listaProductos.clear();
        /*
         * Consulta de los productos activos:
         * - SELECT: columnas del producto, de su categoría y de su marca.
         * - FROM catalogos.productos p: tabla principal (alias p).
         * - INNER JOIN categorias c / marcas m: une cada producto con
         *   su categoría y su marca para obtener sus nombres.
         * - WHERE p.estado = 1: excluye los productos eliminados lógicamente.
         * - ORDER BY p.nombre ASC: ordena alfabéticamente por nombre.
         */
        String sql = """
            SELECT
                p.id_producto,
                p.codigo,
                p.nombre,
                p.descripcion,
                p.precio,
                p.stock,
                p.fecha_creacion,
                c.id_categoria,
                c.nombre AS categoria,
                m.id_marca,
                m.nombre AS marca,
                p.estado
            FROM catalogos.productos p
            INNER JOIN catalogos.categorias c
                ON p.id_categoria = c.id_categoria
            INNER JOIN catalogos.marcas m
                ON p.id_marca = m.id_marca
            WHERE p.estado = 1
            ORDER BY p.nombre ASC
            """;
        // Abre la conexión, prepara y ejecuta la consulta; todo se cierra solo al terminar el try
        try (
                // Obtiene una conexión con PostgreSQL
                Connection conexion =
                        DatabaseConnection.getConnection();
                // Prepara la consulta SELECT
                PreparedStatement ps =
                        conexion.prepareStatement(sql);
                // Ejecuta la consulta y obtiene las filas
                ResultSet rs =
                        ps.executeQuery()
        ) {
            // Recorre cada fila del resultado
            while (rs.next()) {
                // Convierte la fila en un Producto y lo agrega a la lista del TableView
                listaProductos.add(
                        crearProductoDesdeResultSet(rs)
                );
            }
        } catch (SQLException e) {
            // Si ocurre un error de base de datos, se muestra con el detalle de la excepción
            mostrarErrorBaseDatos(
                    "No se pudieron cargar los productos.",
                    e
            );
        }
    }
    /**
     * Verifica si un código ya está registrado en otro producto.
     *
     * No se filtra por estado porque la restricción UNIQUE de
     * PostgreSQL también incluye a los productos desactivados.
     *
     * @param codigo, código que se desea validar
     * @param idExcluir, id del producto que se está actualizando
     *                   (null cuando se registra un producto nuevo)
     * @return true si otro producto ya utiliza el código
     */
    private boolean existeCodigo(String codigo, Integer idExcluir) {
        String sql = """
            SELECT 1
            FROM catalogos.productos
            WHERE codigo = ?
            """;
        /*
         * Al actualizar, se excluye el propio producto para que
         * no se detecte como duplicado su mismo código.
         */
        if (idExcluir != null) {
            sql += """
                AND id_producto <> ?
                """;
        }
        try (
                Connection conexion =
                        DatabaseConnection.getConnection();
                PreparedStatement ps =
                        conexion.prepareStatement(sql)
        ) {
            ps.setString(1, codigo);
            if (idExcluir != null) {
                ps.setInt(2, idExcluir);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            mostrarErrorBaseDatos(
                    "No se pudo verificar el código del producto.",
                    e
            );
            /*
             * Si la verificación falla, PostgreSQL seguirá siendo
             * la autoridad final al intentar guardar.
             */
            return false;
        }
    }
    private boolean validarFormulario(){
        return true;
    }
    /**
     * Muestra una alerta genérica al usuario.
     * @param tipo, tipo de alerta (INFORMATION, WARNING, ERROR, etc.)
     * @param titulo, título de la ventana de la alerta
     * @param mensaje, texto que se le muestra al usuario
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje){
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}