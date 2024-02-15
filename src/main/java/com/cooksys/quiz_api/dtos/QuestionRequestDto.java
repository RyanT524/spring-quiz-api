package com.cooksys.quiz_api.dtos;

import java.util.List;
import com.cooksys.quiz_api.entities.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class QuestionRequestDto {

    private String text;
    
    private List<AnswerRequestDto> answers;
}
