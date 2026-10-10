from enum import Enum
from dataclasses import dataclass
from typing import Optional

class SourceType(str, Enum):
    DOCUMENT = "DOCUMENT"
    API = "API"

@dataclass
class ContextItem:
    text: str
    sourceName: str
    sourceUrl: str
    type: SourceType

class RetrievalInterface:
    def search(self, query: str, limit: int = 5, language: Optional[str] = "en") -> list[ContextItem]:
        """
        Search operation that accepts a query and result limit and returns a list of context items.
        
        Args:
            query (str): The search query.
            limit (int, optional): The maximum number of results to return. Defaults to 5.
            language (str, optional): The language of the query. Defaults to "en".
            
        Returns:
            list[ContextItem]: A list of retrieved context items.
        """
        raise NotImplementedError("This method should be overridden by subclasses.")
