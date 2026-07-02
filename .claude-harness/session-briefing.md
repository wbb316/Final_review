# Session Briefing

## Current State
- Feature: feature-001 (错题本功能) - IMPLEMENTED, needs PR
- Commit: 6d1bd62 - pushed to origin/feature/feature-001
- PR: Not created (gh CLI unavailable)
- Status: needs_review

## Files Created (9)
1. src/main/java/.../entity/WrongQuestion.java - Entity with Lombok
2. src/main/java/.../entity/WrongQuestionVO.java - VO for JOIN query results
3. src/main/java/.../mapper/WrongQuestionMapper.java - MyBatis mapper
4. src/main/resources/mapper/WrongQuestionMapper.xml - SQL with 7 statements
5. src/main/java/.../service/WrongQuestionService.java - recordWrong/recordCorrect logic
6. src/main/java/.../controller/WrongQuestionController.java - REST API (4 endpoints)
7. frontend/src/api/wrongQuestion.js - Axios API module (4 functions)
8. frontend/src/views/WrongQuestionList.vue - Subject-grouped wrong question list
9. frontend/src/views/WrongExamView.vue - Wrong question exam with mastery tracking

## Files Modified (4)
1. src/main/resources/schema.sql - Added wrong_question table with FK + UNIQUE
2. frontend/src/views/ExamView.vue - Fire-and-forget recordWrong on wrong answers
3. frontend/src/views/HomeView.vue - Added "错题本" nav link
4. frontend/src/router/index.js - Added 2 lazy-loaded routes

## Backend API Endpoints
- GET /api/wrong-questions - All wrong questions with question data
- GET /api/wrong-questions/subject/{id} - Filtered by subject
- POST /api/wrong-questions/record-wrong - Record wrong answer (insert/reset)
- POST /api/wrong-questions/record-correct - Record correct answer (increment/remove)

## Key Decisions
- WrongQuestionVO for JOIN query to avoid N+1
- localProgressMap in WrongExamView for accurate per-session tracking
- recordWrong in ExamView is fire-and-forget (non-blocking)

## Next Steps
- Create PR from branch feature/feature-001 (requires gh CLI)
- Merge after review
