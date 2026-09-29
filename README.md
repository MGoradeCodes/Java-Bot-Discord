# Java-Bot-Discord ☕

An intelligent, AI-powered Discord bot written in **Java** using **JDA (Java Discord API)**. The bot functions as an interactive Java programming tutor, leveraging the **Groq AI Engine** (`openai/gpt-oss-120b`) to provide concise, structured code explanations, live web search capabilities, dynamic boilerplate class generation, and intelligent local response caching.

---

## 🤖 Core Features

### 1. AI-Powered Java Tutoring
* **Contextual AI Prompts:** Configured with a dedicated tutor system prompt instructing it to respond professionally using precise Java terminology, concise code snippets, and optional coffee emojis (☕).
* **Automated Character Safety Limits:** Systemic length monitoring keeps all AI outputs below Discord's 2,000-character ceiling to prevent truncation errors.
* **Developer Bypass Mode (`/r`):** An administrative routing mechanism allows the bot owner to toggle off core Java constraints for quick, general-purpose queries.

### 2. Live Web-Search Integration
* **Agentic Browser Search:** Integrated using Groq's `browser_search` tool suite via raw HTTP payload streams.
* **Real-time Answers:** Dynamically crawls the live web for technical documentation or debugging data when target commands or prefix parameters are passed.

### 3. Smart Local Performance Caching
* **JSON File Caching:** Leverages an `ObjectMapper` layout to store and read visual data directly out of `data/localcache.json`.
* **Zero API Latency:** Intercepts identical incoming prompts, bypassing heavy network requests by retrieving exact matches out of the cache.
* (In progress works only for 1 command for testing)

---

## 💻 Command Reference

The bot supports both modern Discord **Slash Commands** (Guild & User App Installs) and classic chat **Prefix Triggers**.

### 🔹 Slash Commands (`/`)

| Command | Parameter(s) | Description |
| :--- | :--- | :--- |
| `/learn` | `concepts` (Dropdown choice) | Instantly teaches core programming pillars (`Strings`, `OOP`, `Recursion`, `Enums`, etc.). |
| `/query` | `query` (Text input) | Asks the Java Tutor AI a customized syntax or conceptual question. |
| `/askwithsearch` | `query` (Text input) | Forces the Groq AI agent to execute a web search before formulating its coding answer. |
| `/createclass` | `name`, `visibility`, `mainmethod` (Y/N) | Dynamically prints out a clean, syntax-highlighted Java boilerplate structure inside standard code blocks. |
| `/java` | `username` (Text input) | A clean introduction tool checking runtime installation viability. |
| `/viewtokens` | *None* | Diagnostics tool querying total tokens spent (`[Prompt, Completion, Total]`) on the operational billing loop. |

### 🔹 Prefix Commands (`q`)
By typing a message beginning with the **`q`** prefix, users can interface with the AI directly from any channel:
* `q <your question>` : Sends a plain request directly through the AI tutoring context pipeline.
* `q /s <your question>` : Explicitly fires up the AI's background thread worker to parse a `browser_search` call over the live web.

---

## 🛠️ Infrastructure Overview

* **Main Framework:** Java Discord API (JDA `v5`) utilizing `GatewayIntent.MESSAGE_CONTENT` and `GatewayIntent.GUILD_MESSAGES`.
* **AI Client Architecture:** Built on top of `OpenAIOkHttpClient` and Java's native `java.net.http.HttpClient` targeting `https://groq.com`.
* **Data Serializer:** Jackson Core Databind (`ObjectMapper`) handling rapid JSON serialization pipelines.

---

## 🔑 Environment Settings

To run the application locally, ensure you export the following secure strings into your host operating system environment:

```bash
export DISCORD_TOKEN="your-discord-bot-token-here"
export GROQ_API_KEY="your-groq-platform-api-key-here"
```
