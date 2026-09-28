# Smart Pantry Manager — Reduce Food Waste

A focused Android utility application built with Java, XML, and SQLite. The Smart Pantry Manager helps users track their at-home ingredients and suggests meals based *strictly* on what they already have — eliminating food waste and unnecessary grocery runs. 

## Features

* **Strict-Matching Recipe Engine** — Suggests recipes only if the user has *every single* required ingredient in their pantry, functioning as the core business logic.
* **Pantry Inventory Management** — Full CRUD (Create, Read, Update, Delete) functionality to track ingredients, quantities, units, and categories.
* **Interactive UI & UX** — Features swipe-to-delete with a unified "Undo" snackbar, category dropdown filtering, and dynamic low-stock warnings (quantities under 2 turn red).
* **Detailed Recipe Views** — Displays perfectly formatted, step-by-step cooking instructions and bulleted ingredient lists in a dedicated Fragment.
* **Offline First** — Pre-loaded with 100 diverse recipes and entirely powered by an on-device database, requiring zero internet connection.

## Database Justification

**Database Chosen: SQLite**
SQLite was chosen as the persistence layer for this application because the Smart Pantry Manager handles structured, relational data (Pantry inventory vs. Recipe requirements). Using `SQLiteOpenHelper` ensures the app functions completely offline, providing lightning-fast, on-device querying. This is essential for the strict-matching algorithm, which needs to iterate through recipes and check them against current inventory without network latency.

## Tech Stack

| Layer | Technology |
| :--- | :--- |
| **Language** | Java |
| **IDE** | Android Studio |
| **Database** | SQLite (`SQLiteOpenHelper`) |
| **UI/Layout** | XML (ConstraintLayout, LinearLayout, CardView) |
| **Navigation** | Jetpack Navigation Component (`NavController`) |
| **Lists** | `RecyclerView` with Custom Adapters |

## Project Structure

```text
app/src/main/
├── java/com/DeandreNaiker/smartpantrymanager/
│   ├── MainActivity.java           # Shared page shell (Bottom Nav host)
│   ├── DatabaseHelper.java         # SQLite config, CRUD operations, & recipe seeding
│   ├── PantryFragment.java         # Inventory view, filtering, and swipe-to-delete
│   ├── RecipesFragment.java        # Core strict-matching algorithm & search
│   ├── RecipeDetailFragment.java   # Dynamic text formatting for recipe steps
│   ├── SettingsFragment.java       # Clear-all functionality
│   ├── PantryAdapter.java          # Binds database cursor to inventory UI
│   └── RecipeAdapter.java          # Binds matching recipes to suggestion UI
└── res/
    ├── layout/                     # XML UI layouts (fragments, items, dialogs)
    ├── navigation/                 # nav_graph.xml (Fragment routing)
    └── values/                     # colors.xml, themes.xml (Light/Dark mode)

