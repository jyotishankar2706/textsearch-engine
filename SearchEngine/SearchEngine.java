import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class SearchEngine extends JFrame {

    // =========================
    // COLORS
    // =========================
    static final Color BG = new Color(232, 239, 248);
    static final Color WHITE = new Color(255, 255, 255);
    static final Color BLUE = new Color(37, 99, 235);
    static final Color DARK_BLUE = new Color(29, 78, 216);
    static final Color LIGHT_BLUE = new Color(235, 243, 255);
    static final Color TEXT = new Color(25, 38, 58);
    static final Color SECONDARY = new Color(91, 108, 130);
    static final Color BORDER = new Color(205, 218, 235);
    static final Color GREEN = new Color(22, 163, 74);

    // =========================
    // SEARCH DATA
    // =========================
    Map<String, Set<String>> invertedIndex = new HashMap<>();
    Map<String, String> documents = new HashMap<>();

    JTextField searchField;
    JPanel resultsPanel;

    JLabel statusLabel;
    JLabel timeLabel;
    JLabel documentLabel;
    JLabel termLabel;

    // =========================
    // MAIN
    // =========================
    public SearchEngine() {

        setTitle("Search Engine v1");
        setSize(1100, 800);
        setMinimumSize(new Dimension(900, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        createUI();
        loadDocuments();
        showWelcome();
    }

    // =========================
    // UI
    // =========================
    private void createUI() {

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        // ---------- HEADER ----------
        JPanel header = new JPanel();
        header.setBackground(WHITE);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel logo = new JLabel("Search Engine v1");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logo.setForeground(BLUE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel smallTitle = new JLabel("Fast • Simple • Intelligent");
        smallTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        smallTitle.setForeground(SECONDARY);
        smallTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(logo);
        header.add(Box.createVerticalStrut(3));
        header.add(smallTitle);

        root.add(header, BorderLayout.NORTH);

        // ---------- MAIN ----------
        JPanel main = new JPanel();
        main.setBackground(BG);
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBorder(new EmptyBorder(35, 50, 25, 50));

        JLabel title = new JLabel("Find anything, instantly.");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        main.add(title);
        main.add(Box.createVerticalStrut(8));

        JLabel subtitle = new JLabel(
                "Search across your indexed documents with speed and precision."
        );
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        main.add(subtitle);
        main.add(Box.createVerticalStrut(25));

        // ---------- SEARCH BAR ----------
        JPanel searchBox = new RoundedPanel(22, WHITE);
        searchBox.setLayout(new BorderLayout(12, 0));
        searchBox.setPreferredSize(new Dimension(850, 72));
        searchBox.setMaximumSize(new Dimension(850, 72));
        searchBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchBox.setBorder(new EmptyBorder(8, 18, 8, 8));

        JLabel searchIcon = new JLabel("⌕");
        searchIcon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 32));
        searchIcon.setForeground(BLUE);
        searchIcon.setHorizontalAlignment(SwingConstants.CENTER);
        searchIcon.setPreferredSize(new Dimension(38, 50));

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        searchField.setForeground(TEXT);
        searchField.setCaretColor(BLUE);
        searchField.setBorder(BorderFactory.createEmptyBorder());
        searchField.setOpaque(false);

        JButton searchButton = new RoundedButton("Search", 17);
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 17));
        searchButton.setForeground(Color.WHITE);
        searchButton.setPreferredSize(new Dimension(130, 54));

        searchButton.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());

        searchBox.add(searchIcon, BorderLayout.WEST);
        searchBox.add(searchField, BorderLayout.CENTER);
        searchBox.add(searchButton, BorderLayout.EAST);

        main.add(searchBox);

        main.add(Box.createVerticalStrut(10));

        JLabel hint = new JLabel(
                "Try: java • machine learning • jav* • pyth? • *learn*"
        );
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hint.setForeground(SECONDARY);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        main.add(hint);

        main.add(Box.createVerticalStrut(22));

        // ---------- STATUS ----------
        JPanel statusBox = new RoundedPanel(18, LIGHT_BLUE);
        statusBox.setLayout(new BoxLayout(statusBox, BoxLayout.Y_AXIS));
        statusBox.setPreferredSize(new Dimension(850, 70));
        statusBox.setMaximumSize(new Dimension(850, 70));
        statusBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusBox.setBorder(new EmptyBorder(10, 20, 10, 20));

        statusLabel = new JLabel("Ready to search");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 21));
        statusLabel.setForeground(DARK_BLUE);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        timeLabel = new JLabel("");
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeLabel.setForeground(SECONDARY);
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusBox.add(statusLabel);
        statusBox.add(Box.createVerticalStrut(2));
        statusBox.add(timeLabel);

        main.add(statusBox);

        main.add(Box.createVerticalStrut(18));

        // ---------- RESULTS ----------
        resultsPanel = new JPanel();
        resultsPanel.setBackground(BG);
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(BG);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setPreferredSize(new Dimension(850, 400));
        scrollPane.setMaximumSize(new Dimension(850, 400));
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);

        main.add(scrollPane);

        root.add(main, BorderLayout.CENTER);

        // ---------- FOOTER ----------
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 8));
        footer.setBackground(new Color(222, 232, 245));

        documentLabel = new JLabel("Documents: 0");
        termLabel = new JLabel("Terms: 0");

        documentLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        termLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        documentLabel.setForeground(DARK_BLUE);
        termLabel.setForeground(DARK_BLUE);

        footer.add(documentLabel);
        footer.add(termLabel);

        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    // =========================
    // LOAD DOCUMENTS
    // =========================
    private void loadDocuments() {

        Path folder = Paths.get(
                System.getProperty("user.dir"),
                "documents"
        );

        try {

            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            try (DirectoryStream<Path> files =
                         Files.newDirectoryStream(folder, "*.txt")) {

                for (Path file : files) {

                    String name = file.getFileName().toString();
                    String content = Files.readString(file);

                    documents.put(name, content);

                    String[] words = tokenize(content);

                    for (String word : words) {

                        invertedIndex
                                .computeIfAbsent(word, k -> new HashSet<>())
                                .add(name);
                    }
                }
            }

            documentLabel.setText(
                    "Documents: " + documents.size()
            );

            termLabel.setText(
                    "Terms: " + invertedIndex.size()
            );

        } catch (IOException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Error loading documents:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =========================
    // TOKENIZE
    // =========================
    private String[] tokenize(String text) {

        text = text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9]+", " ")
                .trim();

        if (text.isEmpty()) {
            return new String[0];
        }

        return text.split("\\s+");
    }

    // =========================
    // SEARCH
    // =========================
    private void performSearch() {

        String query =
                searchField.getText().trim().toLowerCase();

        if (query.isEmpty()) {

            statusLabel.setText("Please enter a search query");
            return;
        }

        long start = System.nanoTime();

        resultsPanel.removeAll();

        String[] terms = query.split("\\s+");

        Set<String> matchedDocuments = new HashSet<>();

        // Find documents for every query term
        for (String term : terms) {

            Set<String> found = findDocuments(term);

            matchedDocuments.addAll(found);
        }

        List<SearchResult> results = new ArrayList<>();

        for (String document : matchedDocuments) {

            int score = calculateScore(
                    document,
                    terms
            );

            results.add(
                    new SearchResult(
                            document,
                            score
                    )
            );
        }

        // Highest relevance first
        results.sort(
                Comparator.comparingInt(
                        (SearchResult r) -> r.score
                ).reversed()
        );

        long end = System.nanoTime();

        double time =
                (end - start) / 1_000_000.0;

        timeLabel.setText(
                String.format("%.2f ms", time)
        );

        if (results.isEmpty()) {

            statusLabel.setText(
                    "✕  Search completed • No documents found"
            );

            createNoResultCard(query);

        } else {

            statusLabel.setText(
                    "✓  Search completed • " +
                            results.size() +
                            " document(s) found"
            );

            for (SearchResult result : results) {

                createResultCard(
                        result.document,
                        result.score,
                        query
                );
            }
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    // =========================
    // FIND DOCUMENTS
    // =========================
    private Set<String> findDocuments(String query) {

        // Normal query
        if (!query.contains("*") &&
                !query.contains("?")) {

            return invertedIndex.getOrDefault(
                    query,
                    Collections.emptySet()
            );
        }

        /*
         * Wildcard rules:
         *
         * * = zero or more characters
         * ? = exactly one character
         *
         * jav*   -> java, javascript, javadoc
         * pyth?  -> python
         * *data* -> data, database, metadata
         */

        String regex = wildcardToRegex(query);

        Pattern pattern = Pattern.compile(
                regex,
                Pattern.CASE_INSENSITIVE
        );

        Set<String> result = new HashSet<>();

        for (String word : invertedIndex.keySet()) {

            if (pattern.matcher(word).matches()) {

                result.addAll(
                        invertedIndex.get(word)
                );
            }
        }

        return result;
    }

    // =========================
    // WILDCARD TO REGEX
    // =========================
    private String wildcardToRegex(String query) {

        StringBuilder regex =
                new StringBuilder("^");

        for (char c : query.toCharArray()) {

            if (c == '*') {

                regex.append(".*");

            } else if (c == '?') {

                regex.append(".");

            } else {

                // Escape regex special characters
                if ("\\.^$|()[]{}+".indexOf(c) >= 0) {
                    regex.append("\\");
                }

                regex.append(c);
            }
        }

        regex.append("$");

        return regex.toString();
    }

    // =========================
    // SCORE
    // =========================
    private int calculateScore(
            String document,
            String[] terms
    ) {

        String content =
                documents.get(document);

        String[] words =
                tokenize(content);

        int score = 0;

        for (String term : terms) {

            if (!term.contains("*") &&
                    !term.contains("?")) {

                for (String word : words) {

                    if (word.equals(term)) {
                        score++;
                    }
                }

            } else {

                Pattern pattern =
                        Pattern.compile(
                                wildcardToRegex(term),
                                Pattern.CASE_INSENSITIVE
                        );

                for (String word : words) {

                    if (pattern.matcher(word).matches()) {
                        score++;
                    }
                }
            }
        }

        return Math.max(score, 1);
    }

    // =========================
    // RESULT CARD
    // =========================
    private void createResultCard(
            String document,
            int score,
            String query
    ) {

        RoundedPanel card =
                new RoundedPanel(
                        20,
                        WHITE
                );

        card.setLayout(
                new BoxLayout(
                        card,
                        BoxLayout.Y_AXIS
                )
        );

        card.setPreferredSize(
                new Dimension(820, 250)
        );

        card.setMaximumSize(
                new Dimension(820, 250)
        );

        card.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        card.setBorder(
                new EmptyBorder(
                        18,
                        22,
                        18,
                        22
                )
        );

        // Document found
        JLabel found =
                new JLabel("✓  DOCUMENT FOUND");

        found.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        found.setForeground(GREEN);

        found.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        card.add(found);

        card.add(Box.createVerticalStrut(6));

        // Document name
        JLabel name =
                new JLabel(document);

        name.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        22
                )
        );

        name.setForeground(DARK_BLUE);

        name.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        card.add(name);

        card.add(Box.createVerticalStrut(10));

        // Content
        String snippet =
                createSnippet(
                        documents.get(document),
                        query
                );

        JTextArea content =
                new JTextArea(snippet);

        content.setEditable(false);
        content.setLineWrap(true);
        content.setWrapStyleWord(true);

        content.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        15
                )
        );

        content.setForeground(TEXT);
        content.setBackground(LIGHT_BLUE);

        content.setBorder(
                new EmptyBorder(
                        12,
                        15,
                        12,
                        15
                )
        );

        content.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        card.add(content);

        card.add(Box.createVerticalStrut(8));

        JLabel relevance =
                new JLabel(
                        "Relevance score: " + score
                );

        relevance.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        relevance.setForeground(SECONDARY);

        relevance.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        card.add(relevance);

        resultsPanel.add(card);

        resultsPanel.add(
                Box.createVerticalStrut(15)
        );
    }

    // =========================
    // SNIPPET
    // =========================
    private String createSnippet(
            String content,
            String query
    ) {

        content =
                content.replaceAll(
                        "\\s+",
                        " "
                ).trim();

        if (content.length() <= 350) {
            return content;
        }

        String[] terms =
                query.split("\\s+");

        int position = -1;

        for (String term : terms) {

            if (!term.contains("*") &&
                    !term.contains("?")) {

                position =
                        content.toLowerCase()
                                .indexOf(term);

                if (position >= 0) {
                    break;
                }
            }
        }

        if (position < 0) {
            return content.substring(0, 350)
                    + "...";
        }

        int start =
                Math.max(
                        0,
                        position - 120
                );

        int end =
                Math.min(
                        content.length(),
                        position + 230
                );

        String snippet =
                content.substring(
                        start,
                        end
                );

        if (start > 0) {
            snippet = "... " + snippet;
        }

        if (end < content.length()) {
            snippet += " ...";
        }

        return snippet;
    }

    // =========================
    // NO RESULT
    // =========================
    private void createNoResultCard(
            String query
    ) {

        RoundedPanel card =
                new RoundedPanel(
                        20,
                        WHITE
                );

        card.setLayout(
                new BoxLayout(
                        card,
                        BoxLayout.Y_AXIS
                )
        );

        card.setPreferredSize(
                new Dimension(820, 150)
        );

        card.setMaximumSize(
                new Dimension(820, 150)
        );

        card.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        card.setBorder(
                new EmptyBorder(
                        25,
                        20,
                        25,
                        20
                )
        );

        JLabel title =
                new JLabel("No document found");

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        22
                )
        );

        title.setForeground(TEXT);
        title.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        JLabel message =
                new JLabel(
                        "No indexed document matches \"" +
                                query +
                                "\"."
                );

        message.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        15
                )
        );

        message.setForeground(SECONDARY);
        message.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(message);

        resultsPanel.add(card);
    }

    // =========================
    // WELCOME
    // =========================
    private void showWelcome() {

        resultsPanel.removeAll();

        RoundedPanel card =
                new RoundedPanel(
                        20,
                        WHITE
                );

        card.setLayout(
                new BoxLayout(
                        card,
                        BoxLayout.Y_AXIS
                )
        );

        card.setPreferredSize(
                new Dimension(820, 180)
        );

        card.setMaximumSize(
                new Dimension(820, 180)
        );

        card.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        card.setBorder(
                new EmptyBorder(
                        28,
                        20,
                        28,
                        20
                )
        );

        JLabel title =
                new JLabel(
                        "Start searching your documents"
                );

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        22
                )
        );

        title.setForeground(TEXT);
        title.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        JLabel text =
                new JLabel(
                        "Enter a keyword above to search your indexed documents."
                );

        text.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        15
                )
        );

        text.setForeground(SECONDARY);
        text.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        JLabel wildcard =
                new JLabel(
                        "Wildcard:  jav*  •  pyth?  •  *data*"
                );

        wildcard.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        14
                )
        );

        wildcard.setForeground(BLUE);
        wildcard.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(text);
        card.add(Box.createVerticalStrut(15));
        card.add(wildcard);

        resultsPanel.add(card);

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    // =========================
    // RESULT CLASS
    // =========================
    static class SearchResult {

        String document;
        int score;

        SearchResult(
                String document,
                int score
        ) {
            this.document = document;
            this.score = score;
        }
    }

    // =========================
    // ROUNDED PANEL
    // =========================
    static class RoundedPanel
            extends JPanel {

        int radius;
        Color color;

        RoundedPanel(
                int radius,
                Color color
        ) {

            this.radius = radius;
            this.color = color;

            setOpaque(false);
        }

        @Override
        protected void paintComponent(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(color);

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    radius,
                    radius
            );

            g2.setColor(BORDER);

            g2.drawRoundRect(
                    0,
                    0,
                    getWidth() - 1,
                    getHeight() - 1,
                    radius,
                    radius
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // =========================
    // ROUNDED BUTTON
    // =========================
    static class RoundedButton
            extends JButton {

        int radius;

        RoundedButton(
                String text,
                int radius
        ) {

            super(text);

            this.radius = radius;

            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);

            setCursor(
                    new Cursor(
                            Cursor.HAND_CURSOR
                    )
            );
        }

        @Override
        protected void paintComponent(
                Graphics g
        ) {

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color color;

            if (getModel().isPressed()) {

                color = DARK_BLUE;

            } else if (
                    getModel().isRollover()
            ) {

                color = new Color(
                        59,
                        130,
                        246
                );

            } else {

                color = BLUE;
            }

            g2.setColor(color);

            g2.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    radius,
                    radius
            );

            g2.dispose();

            super.paintComponent(g);
        }
    }

    // =========================
    // MAIN METHOD
    // =========================
    public static void main(
            String[] args
    ) {

        SwingUtilities.invokeLater(() -> {

            try {
                UIManager.setLookAndFeel(
                        UIManager
                                .getSystemLookAndFeelClassName()
                );
            } catch (Exception ignored) {
            }

            new SearchEngine()
                    .setVisible(true);
        });
    }
}