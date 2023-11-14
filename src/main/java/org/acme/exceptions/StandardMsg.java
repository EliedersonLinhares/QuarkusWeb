package org.acme.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandardMsg {
    private Long timestamp;
    private Integer status;
    private String msg;

}
