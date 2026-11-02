package com.appiancorp.content;

/**
 * Test-only stand-in. The Appian SDK's Document constructor creates one of these, but the real class is not in the SDK
 * jar and needs Appian's object storage. The tests never call it, because document content goes through ContentService.
 */
public class DocumentHelper {
}
