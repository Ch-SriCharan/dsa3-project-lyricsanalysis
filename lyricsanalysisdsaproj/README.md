# LyriSense — Lyrics Similarity & Artist Attribution

**College NLP + Machine Learning Desktop Application**

---

## 📌 1. Project Overview

**LyriSense** is a full-featured Java Swing desktop application developed for college-level Natural Language Processing (NLP) and Machine Learning (ML) research. It solves two primary music intelligence problems:

1. **Lyrics Similarity**: Quantifies textual similarity between unknown/uploaded lyrics and a reference song corpus using **Term Frequency-Inverse Document Frequency (TF-IDF)** and **Cosine Similarity**.
2. **Lyrics Artist Attribution**: Performs **Supervised Text Classification** to predict the stylometric authorship/artist of unknown lyrics with calibrated confidence probabilities and held-out test evaluation.

> [!NOTE]
> **No Fake Results**: All metrics (Accuracy, Precision, Recall, F1-Score, Confusion Matrix, Cosine Similarities, and Artist Probabilities) are calculated via genuine mathematical algorithms and machine learning models executed on the loaded dataset.

---

## 🛠️ 2. Technology Stack & Prerequisites

- **Language**: Java (JDK 17 or higher, compatible with Java 17-26+)
- **GUI Framework**: Java Swing with FlatLaf Modern Dark Theme (`FlatDarkLaf`)
- **Build System**: Apache Maven (`pom.xml`)
- **Compatibility**: VS Code, IntelliJ IDEA, Eclipse, Command Prompt / PowerShell

---

## 🚀 3. How to Run the Application

### Option A: Running from VS Code / Terminal with Maven (Recommended)

1. Open the project folder in **VS Code** (or terminal):
   ```bash
   cd c:\Users\cherr\OneDrive\Documents\lyricsanalysisdsaproj
   ```
2. Compile and execute the project:
   ```bash
   mvn clean compile
   mvn exec:java
   ```

### Option B: Running the Executable Standalone JAR

```bash
mvn package -DskipTests
java -jar target/lyrics-similarity-attribution-1.0.0.jar
```

### Option C: Running Automated Tests

```bash
mvn test
```

---

## 📂 4. Project Directory Structure

```
lyricsanalysisdsaproj/
├── pom.xml                               # Maven Project Object Model configuration
├── README.md                             # Documentation and setup guide
├── sample_lyrics/                        # Sample .txt test lyric files
│   ├── taylor_swift_test.txt
│   ├── eminem_test.txt
│   ├── ed_sheeran_test.txt
│   ├── queen_test.txt
│   ├── adele_test.txt
│   ├── billie_eilish_test.txt
│   └── unknown_song.txt
└── src/
    ├── main/
    │   ├── java/com/lyrics/
    │   │   ├── Main.java                 # Entry point, FlatLaf theme initialization
    │   │   ├── model/
    │   │   │   ├── Song.java             # Song data model & token attributes
    │   │   │   ├── Dataset.java          # Corpus management & dataset distributions
    │   │   │   ├── AnalysisResult.java   # Similarity matches & attribution results
    │   │   │   └── AppContext.java       # Shared application state
    │   │   ├── preprocessing/
    │   │   │   ├── TextPreprocessor.java # Tokenizer, stop-word filter, cleaner
    │   │   │   └── PorterStemmer.java    # Martin Porter stemming algorithm
    │   │   ├── nlp/
    │   │   │   ├── TFIDFVectorizer.java  # Smooth IDF & L2-normalized vectorizer
    │   │   │   └── CosineSimilarity.java # Vector dot product & top-K ranking
    │   │   ├── ml/
    │   │   │   ├── ArtistClassifier.java # Base classifier interface
    │   │   │   ├── MultinomialNBClassifier.java # Naive Bayes with Laplace smoothing
    │   │   │   ├── LogisticRegressionClassifier.java # Softmax Logistic Regression
    │   │   │   ├── ModelEvaluator.java   # Stratified split & no-leakage validation
    │   │   │   └── EvaluationMetrics.java# Metric container & confusion matrix
    │   │   ├── utils/
    │   │   │   ├── CSVReader.java        # RFC 4180 CSV parser & header validator
    │   │   │   └── FileUtils.java        # File reading utilities
    │   │   └── ui/
    │   │       ├── MainFrame.java        # Main window with sidebar navigation
    │   │       ├── HomePanel.java        # Hero screen, credits, feature overview
    │   │       ├── AnalyzePanel.java     # Lyrics upload, paste, similarity, attribution
    │   │       ├── DatasetPanel.java     # CSV upload, statistics, model training
    │   │       ├── EvaluationPanel.java  # Accuracy, Precision, Recall, F1, Confusion Matrix
    │   │       ├── AboutPanel.java       # Math details, tech stack, future scope
    │   │       └── theme/
    │   │           └── UITheme.java      # Color palette, custom buttons & renderers
    │   └── resources/
    │       ├── stopwords.txt             # English stop words list
    │       └── demo_dataset.csv          # Curated reference dataset (6 artists, 48 songs)
    └── test/
        └── java/com/lyrics/
            └── NLPAndMLTest.java         # Automated verification tests (JUnit 5)
```

---

## 📊 5. Reference Dataset CSV Format

Upload any reference dataset with the following mandatory column headers:

```csv
Artist,Song Title,Lyrics
Taylor Swift,Blank Space,"Nice to meet you where you been I could show you incredible things..."
Eminem,Lose Yourself,"Look if you had one shot or one opportunity to seize everything..."
Ed Sheeran,Shape of You,"The club isn't the best place to find a lover so the bar is where I go..."
```

> **Built-in Demo Dataset**: Click **"⚡ Load Demo Dataset"** in the Home or Dataset screen to immediately load 48 curated songs across 6 diverse artists (*Taylor Swift, Eminem, Ed Sheeran, Queen, Billie Eilish, Adele*).

---

## 🔬 6. Core NLP & Machine Learning Mechanics

### A. Text Preprocessing Pipeline (`TextPreprocessor.java`)
1. **Metadata Stripping**: Strips lyric structure tags (e.g., `[Chorus]`, `[Verse 1]`, `(Bridge)`).
2. **Case Normalization**: Converts all text to lowercase.
3. **Punctuation & Character Cleaning**: Removes special symbols and non-alphanumeric noise using regex.
4. **Tokenization**: Splits text into clean word tokens.
5. **Stop-Word Removal**: Filters words matching `stopwords.txt`.
6. **Porter Stemming**: Applies the Porter Stemming Algorithm (`walking` → `walk`, `memories` → `memori`).

### B. TF-IDF Feature Extraction (`TFIDFVectorizer.java`)
- **Term Frequency**:
  $$\text{TF}(t, d) = \text{count}(t, d)$$
- **Smooth Inverse Document Frequency**:
  $$\text{IDF}(t) = \ln\left(\frac{1 + N}{1 + \text{DF}(t)}\right) + 1.0$$
- **L2 Unit Vector Normalization**:
  $$\vec{v}_{\text{norm}} = \frac{\vec{w}}{||\vec{w}||_2}$$
- **Strict Leakage Prevention**: The vectorizer vocabulary and IDF weights are fitted **strictly on the training data / reference corpus**. Uploaded query lyrics are transformed using the **same fitted vectorizer**.

### C. Lyrics Similarity (`CosineSimilarity.java`)
Measures the cosine of the angle between query vector $\vec{A}$ and reference vector $\vec{B}$:
$$\text{CosineSimilarity}(\vec{A}, \vec{B}) = \frac{\vec{A} \cdot \vec{B}}{||\vec{A}||_2 \times ||\vec{B}||_2} = \sum_{j=1}^{V} A_j B_j$$
The application ranks all reference corpus songs and displays the **Top 5** matches with real percentages.

### D. Artist Attribution Classification (`ArtistClassifier.java`)
- **Multinomial Naive Bayes (Laplace Smoothed)**:
  - Class Prior: $P(C_k) = \frac{N_k + \alpha}{N + \alpha K}$
  - Feature Likelihood: $P(w_j | C_k) = \frac{\sum x_{ij} + \alpha}{\sum \sum x_{im} + \alpha V}$
  - Calibrated Posterior: Computed via numerically stable Softmax over log-likelihoods.
- **Softmax Logistic Regression**:
  - Linear Logits: $z_k = \vec{w}_k \cdot \vec{x} + b_k$
  - Probability: $P(y = k | \vec{x}) = \frac{e^{z_k}}{\sum_j e^{z_j}}$
  - Optimization: Batch Gradient Descent with Momentum & $L_2$ Regularization.

### E. Model Evaluation (`ModelEvaluator.java`)
- **Stratified Train/Test Split**: 80% train, 20% test (preserving class representation).
- **Metrics Calculated**:
  - **Accuracy**: $\frac{\text{Correct Test Predictions}}{\text{Total Test Samples}}$
  - **Precision**: $\frac{\text{TP}}{\text{TP} + \text{FP}}$ (Macro & Weighted)
  - **Recall**: $\frac{\text{TP}}{\text{TP} + \text{FN}}$ (Macro & Weighted)
  - **F1-Score**: $2 \times \frac{\text{Precision} \times \text{Recall}}{\text{Precision} + \text{Recall}}$
  - **Confusion Matrix**: $K \times K$ matrix comparing ground-truth test labels against predicted labels.

---

## 🎵 7. Future Scope: MP3 / Audio Support

The core application operates on textual lyrics. Audio/MP3 inputs can be supported in future extensions by integrating an **Automatic Speech Recognition (ASR)** / Speech-to-Text model (e.g. OpenAI Whisper or CMU Sphinx) as an initial acoustic front-end that transcribes vocal audio into text before passing it to LyriSense's NLP and ML pipelines.

---

## 📄 8. License & Academic Integrity

Developed for Academic Demonstration and College NLP/ML coursework. All algorithms are self-contained and implement genuine mathematical formulations without hard-coded outputs.
