from src.contracts import RetrievalInterface, ContextItem, SourceType
from typing import Optional

class MockRetrievalInterface(RetrievalInterface):
    def search(self, query: str, limit: int = 5, language: Optional[str] = "en") -> list[ContextItem]:
        """
        Deterministic mock implementation for integration testing.
        Returns synthetic context items based on the query.
        """
        # Always return deterministic mock data
        results = []
        
        results.append(
            ContextItem(
                text="[MOCK CONTENT - NOT VERIFIED EVIDENCE] Tomato blight is a common disease affecting tomatoes. Treatment involves fungicide application.",
                sourceName="Mock Agricultural DB (Synthetic)",
                sourceUrl="https://mock-db.local/tomato-blight",
                type=SourceType.DOCUMENT
            )
        )
        
        results.append(
            ContextItem(
                text=f"[MOCK CONTENT - NOT VERIFIED EVIDENCE] You searched for: '{query}' in language '{language}'. "
                     "This is a placeholder response for integration testing and should not be used for real agricultural decisions.",
                sourceName="Mock Integration DB",
                sourceUrl="https://mock-db.local/test-query",
                type=SourceType.DOCUMENT
            )
        )
        
        return results[:limit]
