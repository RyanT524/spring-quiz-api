package com.cooksys.quiz_api.services.impl;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.cooksys.quiz_api.dtos.AnswerRequestDto;
import com.cooksys.quiz_api.dtos.QuestionRequestDto;
import com.cooksys.quiz_api.dtos.QuestionResponseDto;
import com.cooksys.quiz_api.dtos.QuizRequestDto;
import com.cooksys.quiz_api.dtos.QuizResponseDto;
import com.cooksys.quiz_api.entities.Answer;
import com.cooksys.quiz_api.entities.Question;
import com.cooksys.quiz_api.entities.Quiz;
import com.cooksys.quiz_api.mappers.QuestionMapper;
import com.cooksys.quiz_api.mappers.QuizMapper;
import com.cooksys.quiz_api.repositories.AnswerRepository;
import com.cooksys.quiz_api.repositories.QuestionRepository;
import com.cooksys.quiz_api.repositories.QuizRepository;
import com.cooksys.quiz_api.services.QuizService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuizMapper quizMapper;
    private final QuestionMapper questionMapper;


    private Quiz getQuiz(Long id) {
        Optional<Quiz> optionalQuiz = quizRepository.findById(id);
        return optionalQuiz.get();
    }


    @Override
    public List<QuizResponseDto> getAllQuizzes() {
        return quizMapper.entitiesToDtos(quizRepository
            .findAllByDeletedFalse());
    }


    @Override
    public QuizResponseDto createQuiz(QuizRequestDto quizRequestDto) {
        Quiz quizToSave = quizMapper.requestDtoToEntity(quizRequestDto);
        quizRepository.saveAndFlush(quizToSave);

        for (QuestionRequestDto questionDto : quizRequestDto.getQuestions()) {
            Question question = new Question();
            question.setText(questionDto.getText());
            question.setQuiz(quizToSave);

            question = questionRepository.saveAndFlush(question);

            for (AnswerRequestDto answerDto : questionDto.getAnswers()) {
                Answer answer = new Answer();
                answer.setText(answerDto.getText());
                answer.setCorrect(answerDto.isCorrect());
                answer.setQuestion(question);

                answerRepository.saveAndFlush(answer);
            }
        }

        return quizMapper.entityToDto(quizToSave);
    }


    @Override
    public QuizResponseDto deleteQuiz(Long id) {
        Quiz quizToDelete = getQuiz(id);
        quizToDelete.setDeleted(true);
        return quizMapper.entityToDto(quizRepository.saveAndFlush(
            quizToDelete));
    }


    @Override
    public QuizResponseDto updateQuizName(Long id, String newName) {

        Optional<Quiz> optionalQuiz = quizRepository.findById(id);
        Quiz quizToUpdate = optionalQuiz.get();

        quizToUpdate.setName(newName);

        return quizMapper.entityToDto(quizRepository.saveAndFlush(
            quizToUpdate));
    }


    @Override
    public QuestionResponseDto getRandomQuestion(Long id) {

        Optional<Quiz> optionalQuiz = quizRepository.findById(id);
        Quiz quizWithQuestion = optionalQuiz.get();

        List<Question> questions = quizWithQuestion.getQuestions();

        Question randomQuestion = questions.get((int)(Math.random() * questions
            .size()));

        return questionMapper.entityToDto(randomQuestion);
    }


    @Override
    public QuizResponseDto addQuestionToQuiz(
        Long id,
        QuestionRequestDto questionRequestDto) {

        Optional<Quiz> optionalQuiz = quizRepository.findById(id);
        Quiz quizToUpdate = optionalQuiz.get();

        Question questionToAdd = questionMapper.questionDtoToEntity(
            questionRequestDto);

        for (Answer answer : questionToAdd.getAnswers()) {
            answer.setQuestion(questionToAdd);
        }

        questionToAdd.setQuiz(quizToUpdate);
        quizToUpdate.getQuestions().add(questionToAdd);

        return quizMapper.entityToDto(quizRepository.saveAndFlush(
            quizToUpdate));

    }


    @Override
    public QuestionResponseDto deleteSpecifiedQuestion(
        Long id,
        Long questionId) {

        Optional<Quiz> optionalQuiz = quizRepository.findById(id);
        Quiz quizToUpdate = optionalQuiz.get();

        Optional<Question> optionalQuestion = questionRepository.findById(
            questionId);
        Question questionToDelete = optionalQuestion.get();

        List<Question> questions = quizToUpdate.getQuestions();
        questions.remove(questionToDelete);
        quizToUpdate.setQuestions(questions);

        for (Answer answer : questionToDelete.getAnswers()) {
            answer.setQuestion(null);
        }

        questionRepository.delete(questionToDelete);

        quizRepository.saveAndFlush(quizToUpdate);

        return questionMapper.entityToDto(questionToDelete);
    }

}
