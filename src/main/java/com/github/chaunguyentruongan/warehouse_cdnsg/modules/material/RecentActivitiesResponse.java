package com.github.chaunguyentruongan.warehouse_cdnsg.modules.material;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivitiesResponse {
    private List<RecentTransactionDTO> recentImports;
    private List<RecentTransactionDTO> recentExports;
}