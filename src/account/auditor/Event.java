package account.auditor;

import lombok.Data;
import lombok.NoArgsConstructor;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @NotNull
    private LocalDateTime date;

    @NotNull
    @NotEmpty
    private String action;

    @NotNull
    @NotEmpty
    private String subject;

    @NotNull
    @NotEmpty
    private String object;

    @NotNull
    @NotEmpty
    private String path;

    public Event(LocalDateTime date,
                 String action,
                 String subject,
                 String object,
                 String path) {

        this.date = date;
        this.action = action;
        this.subject = subject;
        this.object = object;
        this.path = path;
    }
}
