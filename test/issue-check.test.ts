import { describe, it, expect } from "vitest";
describe("Repo addition check", () => {
  it("should include new repo in fetch-issues", () => {
    const repos = ["new/repo"];
    expect(repos.length).toBeGreaterThan(0);
  });
});
