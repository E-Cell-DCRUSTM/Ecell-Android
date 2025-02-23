# Ecell-Android
The official Ecell android app for everything related E-Cell @DCRUSTM

---

## Features

### Market News Dashboard
- **Curated Content:** Displays the latest market news and trends.
- **Offline Caching:** Pre-loads and caches news content for offline access, with updates when connectivity is available using Android's Workmanager API.
- **User-Friendly Interface:** Provides easy filtering and navigation through organized content.

### Meetings Module
- **Schedule Display:** Lists upcoming meetings with details such as time, venue, and host.
- **Meeting Summaries:** Offers neat summaries and key takeaways from past meetings.
- **Push Notifications:** Sends reminders for upcoming events.

### Quiz with Buzzer System
- **Interactive Sessions:** Hosts live quizzes where participants can join using a buzzer.
- **Latency Check:** Determines precisely for latency before starting the quiz.
- **Real-Time Feedback:** Delivers instant quiz responses and scoring updates.

### Local Scoreboard Website
- **Live Score Display:** Serves a local website that showcases real-time quiz scores on larger screens.
- **Responsive Design:** Optimized for projectors, monitors, and various screen sizes.
- **Seamless Access:** Accessible from any device connected to the local network.

---

## Architecture: Clean Architecture

ECell is built on Clean Architecture principles with usecases to streamline the multiple components, ensuring separation of concerns and scalability.

## Roadmap

### Phase 1: Core Features
- [ ] Setup a REST API server to gather and serve market related news in the app.
- [ ] **Market News Dashboard:** Implement offline caching and content display.
- [ ] **Meetings Module:** Develop scheduling, notifications, and summary functionalities which can be done by an Admin.
- [ ] **Quiz Feature:** Build interactive quizzes with a real-time buzzer system using server based approach as of now.
- [ ] **Local Scoreboard:** Create a locally hosted website for live score display using nano httpd library.

### Phase 2: Enhancements
- [ ] Refine UI/UX for better engagement.
- [ ] Consider an offline network based quiz featuers.

### Phase 3: Future Expansion
- [ ] Port to Kotlin Multi platform project for cross-platform(iOS Support as well).
- [ ] Integrate external APIs for dynamic market news.
- [ ] Expand scalability for multi-location and larger events.
- [ ] Connectivity features for E-Cell alumnus network.

---

## Contributing

Contributions are welcome! To contribute:

1. Fork the repository.
2. Create a feature or resolve an issue.
3. Commit your changes and push to your branch.
4. Open a pull request for review.

if you find any bugs simply file an issue in the repository as soon as possible. That would be really helpful.

