package org.aptech.backendmypham.services;

import org.aptech.backendmypham.models.Branch;

import java.util.List;

public interface BranchService {
    List<Branch> getALlBranch();
    Branch createBranch(Branch branch);
    Branch findByName(String branchName);
    Branch findByID(Long id);
    void updateBranch(Long BiD,Branch updatedBranch);
    void deleteBranch(Long BiD);
}
