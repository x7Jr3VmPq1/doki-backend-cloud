package com.megrez.rabbit.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CountMessage {
    Integer uid;
    Integer count;
}


