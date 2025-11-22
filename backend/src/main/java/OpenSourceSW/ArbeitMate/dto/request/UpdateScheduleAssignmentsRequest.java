package OpenSourceSW.ArbeitMate.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UpdateScheduleAssignmentsRequest {
    @NotEmpty private List<Item> items;

    @Data
    public static class Item {
        @NotNull private UUID scheduleId;
        @NotEmpty private List<UUID> memberIds;
    }
}
