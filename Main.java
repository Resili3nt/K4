import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

// Класс Employee для представления данных сотрудника
class Employee {
    private String name;
    private int age;
    private String department;

    public Employee(String name, int age, String department) {
        this.name = name;
        this.age = age;
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getDepartment() {
        return department;
    }

    public Object[] toTableRow() {
        return new Object[]{name, age, department};
    }
}

// Класс EmployeeManager для управления сотрудниками и выполнения операций
class EmployeeManager {
    private List<Employee> employees;

    public EmployeeManager() {
        employees = new ArrayList<>();
    }

    public void readFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            employees.clear();  // Очистка списка перед загрузкой новых данных
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 3) {
                    String name = data[0].trim();
                    int age = Integer.parseInt(data[1].trim());
                    String department = data[2].trim();
                    employees.add(new Employee(name, age, department));
                }
            }
            JOptionPane.showMessageDialog(null, "Данные успешно загружены из файла.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при чтении файла: " + e.getMessage());
        }
    }

    public void saveToFile(String filename) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            for (Employee employee : employees) {
                bw.write(employee.getName() + "," + employee.getAge() + "," + employee.getDepartment());
                bw.newLine();
            }
            JOptionPane.showMessageDialog(null, "Данные успешно сохранены в файл.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при сохранении файла: " + e.getMessage());
        }
    }

    public void addEmployee(Employee employee) {
        employees.add(employee);
    }

    public void removeEmployee(int index) {
        if (index >= 0 && index < employees.size()) {
            employees.remove(index);
        }
    }

    public void sortByAge() {
        employees.sort(Comparator.comparingInt(Employee::getAge));
    }

    public List<Employee> filterByDepartment(String department) {
        return employees.stream()
                .filter(e -> e.getDepartment().equalsIgnoreCase(department))
                .collect(Collectors.toList());
    }

    public List<Employee> getEmployees() {
        return employees;
    }
}

// Класс Main с графическим интерфейсом
public class Main extends JFrame {
    private EmployeeManager manager;
    private JTable employeeTable;
    private DefaultTableModel tableModel;

    public Main() {
        manager = new EmployeeManager();
        tableModel = new DefaultTableModel(new Object[]{"Имя", "Возраст", "Отдел"}, 0);
        employeeTable = new JTable(tableModel);
        employeeTable.setFillsViewportHeight(true);

        // Установка параметров окна
        setTitle("Управление сотрудниками");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Панель с кнопками
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(8, 1, 5, 5));

        // Создание кнопок с переводом на русский
        JButton readButton = new JButton("Загрузить из файла");
        JButton saveButton = new JButton("Сохранить в файл");
        JButton addButton = new JButton("Добавить сотрудника");
        JButton deleteButton = new JButton("Удалить сотрудника");
        JButton displayButton = new JButton("Показать всех");
        JButton sortButton = new JButton("Сортировать по возрасту");
        JButton filterButton = new JButton("Фильтровать по отделу");

        // Добавление кнопок на панель
        panel.add(readButton);
        panel.add(saveButton);
        panel.add(addButton);
        panel.add(deleteButton);
        panel.add(displayButton);
        panel.add(sortButton);
        panel.add(filterButton);

        // Добавление панели и таблицы в окно
        add(panel, BorderLayout.WEST);
        add(new JScrollPane(employeeTable), BorderLayout.CENTER);

        // Обработчики событий для кнопок
        readButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                manager.readFromFile(file.getAbsolutePath());
                updateTable(manager.getEmployees());
            }
        });

        saveButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String filePath = file.getAbsolutePath();
                
                // Проверка на расширение .txt, добавляем его, если отсутствует
                if (!filePath.toLowerCase().endsWith(".txt")) {
                    filePath += ".txt";
                }
                manager.saveToFile(filePath);
            }
        });

        addButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog("Введите имя:");
            if (name == null || name.isEmpty()) return;
            String ageString = JOptionPane.showInputDialog("Введите возраст:");
            if (ageString == null || ageString.isEmpty()) return;
            String department = JOptionPane.showInputDialog("Введите отдел:");
            if (department == null || department.isEmpty()) return;

            try {
                int age = Integer.parseInt(ageString);
                manager.addEmployee(new Employee(name, age, department));
                updateTable(manager.getEmployees());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Некорректный возраст. Пожалуйста, введите число.");
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = employeeTable.getSelectedRow();
            if (selectedRow != -1) {
                int modelIndex = employeeTable.convertRowIndexToModel(selectedRow);
                manager.removeEmployee(modelIndex);
                updateTable(manager.getEmployees());
                JOptionPane.showMessageDialog(null, "Сотрудник удален.");
            } else {
                JOptionPane.showMessageDialog(null, "Пожалуйста, выберите строку для удаления.");
            }
        });

        displayButton.addActionListener(e -> updateTable(manager.getEmployees()));

        sortButton.addActionListener(e -> {
            manager.sortByAge();
            updateTable(manager.getEmployees());
        });

        filterButton.addActionListener(e -> {
            String department = JOptionPane.showInputDialog("Введите отдел для фильтрации:");
            if (department != null && !department.isEmpty()) {
                List<Employee> filteredEmployees = manager.filterByDepartment(department);
                updateTable(filteredEmployees);
            }
        });
    }

    // Метод для обновления данных в таблице
    private void updateTable(List<Employee> employees) {
        tableModel.setRowCount(0);  // Очищаем таблицу
        for (Employee employee : employees) {
            tableModel.addRow(employee.toTableRow());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main frame = new Main();
            frame.setVisible(true);
        });
    }
}
